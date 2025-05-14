package me.drawn.gui.custom;

import me.drawn.gui.*;
import me.drawn.management.VerseGeneratorManager;
import me.drawn.management.VerseWorldManager;
import me.drawn.management.entities.VerseCreationOptions;
import me.drawn.management.entities.VerseWorld;
import me.drawn.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static me.drawn.gui.VerseGUI.simpleButton;

public class WorldCreationGUI {

    public static final HashMap<UUID, VerseCreationOptions> verseCreatorHashMap = new HashMap<>();
    public static final ArrayList<Inventory> inventories = new ArrayList<>();

    // Opens the Environment Selection Menu, then open the main menu
    public static void openMenu(Player player, final String worldName) {
        verseCreatorHashMap.put(player.getUniqueId(), new VerseCreationOptions(worldName));
        player.openInventory(EnvironmentGUI.inventory);

        VerseGUI.setCurrentMenu(player, VerseGUI.Type.CREATION_GUI);
    }

    public static void onClick(final int slot, final Player player, final Inventory inv, VerseGUI.Type type) {
        VerseCreationOptions creator = verseCreatorHashMap.get(player.getUniqueId());

        switch (slot) {
            // World creation
            case 4: {
                player.closeInventory();

                player.sendTitle(Utils.GREEN_COLOR + "Creating world...",
                        "§7The server is creating the world, please wait", 5, 240, 0);

                VerseWorldManager.createWorld(creator, new VerseWorldManager.WorldCreationCallback() {
                    @Override
                    public void onWorldCreate(VerseWorld world) {
                        player.sendTitle(Utils.GREEN_COLOR + "World created!", " ", 0, 40, 10);
                        world.teleport(player);

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                        VerseGUI.deleteCache(player, inv);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Utils.formalPlayerWarning(player, "An error occurred while trying to create your world: " + exception.getMessage() + " More information printed out to the server console.");
                        exception.fillInStackTrace();

                        VerseGUI.deleteCache(player, inv);
                    }
                });

                break;
            }
            // World Type
            case 19: {
                player.openInventory(WorldTypesGUI.inventory);
                break;
            }
            // Seed
            case 21: {
                Utils.awaitChatInput(player, new Utils.ChatInputCallback() {
                    @Override
                    public void onInput(String response) {
                        try {
                            long l = Long.parseLong(response);

                            creator.seed(l);
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                        } catch (Exception ex) {
                            Utils.formalPlayerWarning(player, "World seeds must be a valid number!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        }

                        openMainMenu(player);
                    }

                    @Override
                    public void onCancel() {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        openMainMenu(player);
                    }
                });
                break;
            }
            // World Generator
            case 23: {
                player.openInventory(GeneratorsGUI.inventory);
                break;
            }
            // World Generator Custom ID
            case 32: {
                if (inv.getItem(slot) == null)
                    return;

                Utils.awaitChatInput(player, new Utils.ChatInputCallback() {
                    @Override
                    public void onInput(String response) {
                        creator.chunkGenerator(creator.chunkGenerator(), response);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);

                        openMainMenu(player);
                    }

                    @Override
                    public void onCancel() {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        openMainMenu(player);
                    }
                });
                break;
            }
            // Hardcore
            case 25: {
                creator.hardcore(!creator.hardcore());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                openMainMenu(player);
                break;
            }
        }
    }

    public static void selectEnvironment(Player player, World.Environment environment) {
        verseCreatorHashMap.get(player.getUniqueId()).environment(environment);

        player.closeInventory();
        openMainMenu(player);
    }

    public static void openMainMenu(Player player) {
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

        inventory.setItem(21, simpleButton(Material.WHEAT_SEEDS, "Seed", Utils.GREEN_COLOR+"Current seed: &f"+creator.seed(), " ", "Click to set a custom seed", "for the world."));

        inventory.setItem(23, simpleButton(Material.BEACON, "World Generators", Utils.GREEN_COLOR+"Current generator: &f"+generatorName, " ", "Click to select a custom", "world generator.", " ",
                "Currently there is &a"+ VerseGeneratorManager.getAllGenerators().size()+" &7registered", "custom world generators."));

        if(creator.hasChunkGenerator() && !creator.getGeneratorFullName().contains("MegaVerse")) {
            inventory.setItem(32, simpleButton(Material.NAME_TAG, "Generator ID", Utils.GREEN_COLOR+"Full generator name: &f"+creator.getGeneratorFullName(), " ", "Some plugins requires an ID or parameter", "alongside it's name to work or load custom packs.", "This is the case in plugins like Terra and RTG."));
        }

        inventory.setItem(25, simpleButton(Material.SKELETON_SKULL, "Hardcore Mode", Utils.GREEN_COLOR+"Enabled?: &f"+hardcoreEnabled, " ",
                "Click to toggle hardcore", "mode for this world.", " ", "Enabling this option will mark the", "world as a Hardcore Mode world.", "In order to fully play Hardcore, you need to enable", "it inside your server configuration file aswell."));

        player.openInventory(inventory);

        inventories.add(inventory);
    }
}