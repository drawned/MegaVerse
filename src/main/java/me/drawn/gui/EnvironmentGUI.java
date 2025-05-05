package me.drawn.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import static me.drawn.utils.MenuUtils.simpleButton;

public class EnvironmentGUI {

    public static final Inventory inventory = generateInventory();
    private static Inventory generateInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27, "Select the world environment");
        inventory.setItem(10, simpleButton(Material.NETHERRACK, "Nether", "Click to select the world type", "as a Nether type."));

        inventory.setItem(13, simpleButton(Material.GRASS_BLOCK, "Overworld", "Click to select the world type", "as the normal Overworld type."));

        inventory.setItem(16, simpleButton(Material.END_STONE, "The End", "Click to select the world type", "as The End type."));

        return inventory;
    }

}
