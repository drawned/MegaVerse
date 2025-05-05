package me.drawn.gui;

import me.drawn.MegaVerse;
import me.drawn.management.VerseGeneratorManager;
import me.drawn.management.VerseWorldManager;
import me.drawn.management.entities.CustomGenerator;
import me.drawn.management.entities.VerseCreationOptions;
import me.drawn.management.entities.VerseWorld;
import me.drawn.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static me.drawn.utils.MenuUtils.simpleButton;

public class WorldCreationGUI implements Listener {

    public static HashMap<UUID, VerseCreationOptions> verseCreatorHashMap = new HashMap<>();

    private static final ArrayList<Inventory> creationInventories = new ArrayList<>();

    public static void openSelectMenu(Player player, final String worldName) {
        verseCreatorHashMap.put(player.getUniqueId(), new VerseCreationOptions(worldName));
        player.openInventory(EnvironmentGUI.inventory);
    }

    public static boolean isVerseGUI(final Inventory inventory) {
        return creationInventories.contains(inventory)
                || inventory == GeneratorsGUI.inventory
                || inventory == EnvironmentGUI.inventory
                || inventory == WorldTypesGUI.inventory;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        final Inventory inventory = e.getInventory();

        if(inventory == GeneratorsGUI.inventory
        || inventory == WorldTypesGUI.inventory) {
            Bukkit.getScheduler().runTaskLater(MegaVerse.getInstance(), () -> {
                openMainMenu((Player) e.getPlayer());
            }, 2);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        final int slot = e.getSlot();
        final Player p = (Player)e.getWhoClicked();
        final Inventory inventory = e.getInventory();

        if(!isVerseGUI(inventory))
            return;

        e.setCancelled(true);
        p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1);

        // World generators selection
        if(inventory == GeneratorsGUI.inventory) {
            if(!verseCreatorHashMap.containsKey(p.getUniqueId()))
                return;

            CustomGenerator gen = GeneratorsGUI.getGeneratorInSlot(slot);

            if(gen != null) {
                verseCreatorHashMap.get(p.getUniqueId()).chunkGenerator(gen);
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            }

            p.closeInventory();
            openMainMenu(p);

            return;
        }

        // World Types selection
        if(inventory == WorldTypesGUI.inventory) {
            if(!verseCreatorHashMap.containsKey(p.getUniqueId()))
                return;

            WorldType type = WorldTypesGUI.getWorldTypeInSlot(slot);

            verseCreatorHashMap.get(p.getUniqueId()).type(type);
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);

            p.closeInventory();
            openMainMenu(p);

            return;
        }

        // Environment selection
        if(inventory == EnvironmentGUI.inventory) {
            switch (slot) {
                case 10: {
                    selectEnvironment(p, World.Environment.NETHER);
                    return;
                }
                case 13: {
                    selectEnvironment(p, World.Environment.NORMAL);
                    return;
                }
                case 16: {
                    selectEnvironment(p, World.Environment.THE_END);
                    return;
                }
            }
        }

        if(creationInventories.contains(inventory)) {
            VerseCreationOptions creator = verseCreatorHashMap.get(p.getUniqueId());
            switch (slot) {
                // World creation
                case 4: {
                    p.closeInventory();

                    p.sendTitle(Utils.GREEN_COLOR+"Creating world...",
                            "§7The server is creating the world, please wait", 5, 240, 0);

                    VerseWorldManager.createWorld(creator, new VerseWorldManager.WorldCreationCallback() {
                        @Override
                        public void onWorldCreate(VerseWorld world) {
                            p.sendTitle(Utils.GREEN_COLOR+"World created!", " ", 0, 40, 10);
                            world.teleport(p);

                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                            deleteCache(p, inventory);
                        }

                        @Override
                        public void onError(Exception exception) {
                            Utils.formalPlayerWarning(p, "An error occurred while trying to create your world: "+exception.getMessage()+" More information printed out to the server console.");
                            exception.fillInStackTrace();

                            deleteCache(p, inventory);
                        }
                    });

                    break;
                }
                // World Type
                case 19: {
                    p.openInventory(WorldTypesGUI.inventory);
                    break;
                }
                // Seed
                case 21: {
                    Utils.awaitChatInput(p, new Utils.ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            try {
                                long l = Long.parseLong(response);

                                creator.seed(l);
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                            } catch (Exception ex) {
                                Utils.formalPlayerWarning(p, "World seeds must be a valid number!");
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            }

                            openMainMenu(p);
                        }

                        @Override
                        public void onCancel() {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            openMainMenu(p);
                        }
                    });
                    break;
                }
                // World Generator
                case 23: {
                    p.openInventory(GeneratorsGUI.inventory);
                    break;
                }
                // World Generator Custom ID
                case 32: {
                    if(inventory.getItem(slot) == null)
                        return;

                    Utils.awaitChatInput(p, new Utils.ChatInputCallback() {
                        @Override
                        public void onInput(String response) {
                            creator.chunkGenerator(creator.chunkGenerator(), response);
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);

                            openMainMenu(p);
                        }

                        @Override
                        public void onCancel() {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            openMainMenu(p);
                        }
                    });
                    break;
                }
                // Hardcore
                case 25: {
                    creator.hardcore(!creator.hardcore());
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                    openMainMenu(p);
                    break;
                }
            }
        }
    }

    private static void deleteCache(Player player, Inventory mainMenu) {
        creationInventories.remove(mainMenu);
        verseCreatorHashMap.remove(player.getUniqueId());
    }

    private static void selectEnvironment(Player player, World.Environment environment) {
        player.closeInventory();

        verseCreatorHashMap.get(player.getUniqueId()).environment(environment);

        openMainMenu(player);
    }

    private static void openMainMenu(Player player) {
        VerseCreationOptions creator = verseCreatorHashMap.get(player.getUniqueId());

        Inventory inventory = Bukkit.createInventory(null, 36, "Configure the world options");

        final String generatorName = (creator.hasChunkGenerator() ? creator.chunkGenerator().getReadableName() : "Vanilla");

        final String hardcoreEnabled = (creator.hardcore() ? "&4Yes" : "No (default)");

        inventory.setItem(4, simpleButton(Material.LIME_DYE, "Create World", "Click to finish the world creation", " ",
                Utils.GREEN_COLOR+"World Name: &f"+creator.worldName(),
                Utils.GREEN_COLOR+"World Environment: &f"+creator.environment().name(),
                " ",
                Utils.GREEN_COLOR+"World Seed: &f"+creator.seed(),
                Utils.GREEN_COLOR+"World Type: &f"+creator.type(),
                Utils.GREEN_COLOR+"World Generator: &f"+generatorName,
                "&cHardcore Mode?: &f"+hardcoreEnabled
                ));

        inventory.setItem(19, simpleButton(Material.GRASS_BLOCK, "World Type", Utils.GREEN_COLOR+"Current Type: &f"+creator.type().name(), " ",
                "Click to select the type", "of the world.",
                " ", "Available options:", "• Normal", "• Flat", "• Amplified", "• Large Biomes"));

        inventory.setItem(21, simpleButton(Material.WHEAT_SEEDS, "Seed", "Click to set a custom seed", "for the world."));

        inventory.setItem(23, simpleButton(Material.BEACON, "World Generators", Utils.GREEN_COLOR+"Current generator: &f"+generatorName, " ", "Click to select a custom", "world generator.", " ",
                "Currently there is &a"+ VerseGeneratorManager.getAllGenerators().size()+" &7registered", "custom world generators."));

        if(creator.hasChunkGenerator() && !creator.getGeneratorFullName().contains("MegaVerse")) {
            inventory.setItem(32, simpleButton(Material.NAME_TAG, "Generator ID", Utils.GREEN_COLOR+"Full generator name: &f"+creator.getGeneratorFullName(), " ", "Some plugins requires an ID or parameter", "alongside it's name to work or load custom packs.", "This is the case in plugins like Terra and RTG."));
        }

        inventory.setItem(25, simpleButton(Material.SKELETON_SKULL, "Hardcore Mode", Utils.GREEN_COLOR+"Enabled?: &f"+hardcoreEnabled, " ",
                "Enabling this option will mark the", "world as a Hardcore Mode world", "In order to fully play Hardcore, you need to enable", "it inside your server configuration file aswell.", " ", "Click to toggle hardcore", "mode for this world."));

        player.openInventory(inventory);

        creationInventories.add(inventory);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if(isVerseGUI(e.getInventory()))
            e.setCancelled(true);
    }

}