package me.drawn.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.WorldType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static me.drawn.utils.MenuUtils.simpleButton;

public class WorldTypesGUI {

    public static final Inventory inventory = generateInventory();
    private static Inventory generateInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, "");

        inv.setItem(10, simpleButton(Material.GRASS, "Normal", "The default world type", "of a vanilla Minecraft world"));

        inv.setItem(12, simpleButton(Material.OAK_SLAB, "Flat", "The classic Flat world type", "with just the Plains biome."));

        inv.setItem(14, simpleButton(Material.GRASS_BLOCK, "Amplified", "This is a variant of the Normal", "world type where biomes are amplified", "in height."));

        inv.setItem(16, simpleButton(Material.WATER_BUCKET, "Large Biomes", "This is another variant of the Normal", "world type, but biomes are", "significantly larger in size."));

        return inv;
    }

    @NotNull
    public static WorldType getWorldTypeInSlot(final int slot) {
        WorldType type = WorldType.NORMAL;
        switch (slot) {
            case 10: {
                type = WorldType.NORMAL;
                break;
            }
            case 12: {
                type = WorldType.FLAT;
                break;
            }
            case 14: {
                type = WorldType.AMPLIFIED;
                break;
            }
            case 16: {
                type = WorldType.LARGE_BIOMES;
                break;
            }
        }
        return type;
    }

}
