package me.drawn.gui;

import me.drawn.gui.custom.WorldCreationGUI;
import me.drawn.gui.custom.WorldImportGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static me.drawn.gui.VerseGUI.simpleButton;

public class EnvironmentGUI {

    public static final Inventory inventory = generateInventory();
    private static Inventory generateInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27, "Select the world environment");
        inventory.setItem(10, simpleButton(Material.NETHERRACK, "Nether", "Click to select the world type", "as a Nether type."));

        inventory.setItem(13, simpleButton(Material.GRASS_BLOCK, "Overworld", "Click to select the world type", "as the normal Overworld type."));

        inventory.setItem(16, simpleButton(Material.END_STONE, "The End", "Click to select the world type", "as The End type."));

        return inventory;
    }

    public static void onClick(final int slot, final Player player, final Inventory inv, VerseGUI.Type type) {
        World.Environment environment = getWorldEnvironmentInSlot(slot);

        if(type == VerseGUI.Type.CREATION_GUI)
            WorldCreationGUI.selectEnvironment(player, environment);
        else
            WorldImportGUI.selectEnvironment(player, environment);
    }

    public static World.Environment getWorldEnvironmentInSlot(final int slot) {
        switch (slot) {
            case 10: {return World.Environment.NETHER;}
            case 13: {return World.Environment.NORMAL;}
            case 16: {return World.Environment.THE_END;}
        }
        return World.Environment.NORMAL;
    }
}
