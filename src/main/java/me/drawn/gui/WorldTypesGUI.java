package me.drawn.gui;

import me.drawn.gui.custom.WorldCreationGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static me.drawn.gui.VerseGUI.simpleButton;

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

    public static void onClick(final int slot, final Player player, final Inventory inv, VerseGUI.Type type) {
        if (type == VerseGUI.Type.CREATION_GUI) {
            if (!WorldCreationGUI.verseCreatorHashMap.containsKey(player.getUniqueId()))
                return;

            WorldType worldType = WorldTypesGUI.getWorldTypeInSlot(slot);

            WorldCreationGUI.verseCreatorHashMap.get(player.getUniqueId()).type(worldType);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);

            player.closeInventory();
            WorldCreationGUI.openMainMenu(player);
        }
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
