package me.drawn.gui.custom;

import me.drawn.gui.EnvironmentGUI;
import me.drawn.gui.VerseGUI;
import me.drawn.management.VerseGeneratorManager;
import me.drawn.management.VerseImportManager;
import me.drawn.management.VerseWorldManager;
import me.drawn.management.entities.VerseCreationOptions;
import me.drawn.management.entities.VerseWorld;
import me.drawn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static me.drawn.gui.VerseGUI.simpleButton;

public class WorldImportGUI {

    public static final HashMap<UUID, VerseImportManager.ImportOptions> importOptionsHashMap = new HashMap<>();

    public static final HashMap<UUID, VerseCreationOptions> verseCreatorHashMap = new HashMap<>();
    public static final ArrayList<Inventory> inventories = new ArrayList<>();

    // Opens the Environment Selection Menu, then open the main menu
    public static void openMenu(Player player, @NotNull File worldFolder) {
        VerseCreationOptions creationOptions = new VerseCreationOptions(worldFolder.getName());

        verseCreatorHashMap.put(player.getUniqueId(), creationOptions);

        try {
            VerseImportManager.ImportOptions importOptions = new VerseImportManager.ImportOptions(worldFolder);
            if(importOptions.wasSet()) {
                creationOptions.hardcore(importOptions.isHardcoreMode());
                importOptionsHashMap.put(player.getUniqueId(), importOptions);
            }
        } catch (IOException ignored) {}

        player.openInventory(EnvironmentGUI.inventory);
        VerseGUI.setCurrentMenu(player, VerseGUI.Type.IMPORT_GUI);
    }

    public static void selectEnvironment(Player player, World.Environment environment) {
        verseCreatorHashMap.get(player.getUniqueId()).environment(environment);

        player.closeInventory();
        openMainMenu(player);
    }

    public static void onClick(final int slot, final Player player, final Inventory inv, VerseGUI.Type type) {
        VerseCreationOptions creator = verseCreatorHashMap.get(player.getUniqueId());

        switch (slot) {
            case 22: {
                Utils.awaitChatInput(player, new Utils.ChatInputCallback() {
                    @Override
                    public void onInput(String response) {
                        creator.chunkGenerator(response);
                        openMainMenu(player);
                    }

                    @Override
                    public void onCancel() {
                        openMainMenu(player);
                    }
                });

                return;
            }
            // Import world
            case 5: {
                player.closeInventory();

                player.sendTitle(Utils.GREEN_COLOR + "Importing world...",
                        "§7The server is importing the world, please wait", 5, 240, 0);

                VerseWorldManager.createWorld(creator, new VerseWorldManager.WorldCreationCallback() {
                    @Override
                    public void onWorldCreate(VerseWorld world) {
                        player.sendTitle(Utils.GREEN_COLOR + "World imported and loaded!", " ", 0, 40, 10);
                        world.teleport(player);

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Utils.formalPlayerWarning(player, "An error occurred while trying to import your world: " + exception.getMessage() + " More information printed out to the server console.");
                        exception.fillInStackTrace();
                    }
                });

                VerseGUI.deleteCache(player, inv);
                return;
            }
        }

    }

    private static void openMainMenu(Player player) {
        VerseCreationOptions creator = verseCreatorHashMap.get(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 27, "Importing world "+creator.worldName());

        final String generatorName = (creator.getGeneratorFullName() == null ? "Vanilla" : creator.getGeneratorFullName());

        VerseImportManager.ImportOptions importOptions = importOptionsHashMap.get(player.getUniqueId());
        if(importOptions != null && importOptions.wasSet()) {
            inv.setItem(3, simpleButton(Material.PAPER, "Detected World Configuration", "Detected from the world directory.", " ",
                    Utils.GREEN_COLOR+"Minecraft Version: &f"+importOptions.getVersion(),
                    Utils.GREEN_COLOR+"Last Time Played: &f"+importOptions.getLastPlayed(),
                    " ",
                    Utils.GREEN_COLOR+"Vanilla World: &f"+(importOptions.wasModded() ? "No" : "Yes"),
                    Utils.GREEN_COLOR+"World Type: &f"+importOptions.getWorldType(),
                    " ",
                    Utils.GREEN_COLOR+"Difficulty: &f"+importOptions.getDifficulty(),
                    Utils.GREEN_COLOR+"Hardcore Mode?: &f"+(importOptions.isHardcoreMode() ? "Yes" : "No"),
                    Utils.GREEN_COLOR+"Raining: &f"+(importOptions.isRaining() ? "Yes" : "No")
            ));
        }

        inv.setItem(5, simpleButton(Material.LIME_DYE, "Import World", "Click to finish and import this world.",
                " ",
                Utils.WARNING_COLOR+"If you are importing a world that",
                Utils.WARNING_COLOR+"uses Custom World Generators, you",
                Utils.WARNING_COLOR+"&lMUST"+Utils.WARNING_COLOR+" provide them in this menu before",
                Utils.WARNING_COLOR+"trying to import the world!",
                " ",
                Utils.GREEN_COLOR+"World Name: &f"+creator.worldName(),
                Utils.GREEN_COLOR+"World Environment: &f"+creator.environment().name(),
                " ",
                Utils.GREEN_COLOR+"World Generator: &f"+generatorName
        ));

        inv.setItem(22, simpleButton(Material.BEACON, "World Generator", Utils.GREEN_COLOR+"Current generator: &f"+generatorName, " ",
                "In case there's any, you must provide", "the custom generator this world uses.", " ",
                "Currently there is &a"+ VerseGeneratorManager.getAllGenerators().size()+" &7registered", "custom world generators."));

        inventories.add(inv);

        player.openInventory(inv);
    }
}
