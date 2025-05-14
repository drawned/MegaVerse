package me.drawn.gui;

import me.drawn.MegaVerse;
import me.drawn.gui.custom.WorldCreationGUI;
import me.drawn.gui.custom.WorldImportGUI;
import me.drawn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VerseGUI implements Listener {

    public enum Type {
        CREATION_GUI,
        IMPORT_GUI
    }
    private static final HashMap<UUID, Type> currentMenu = new HashMap<>();

    public static void setCurrentMenu(Player player, Type type) {
        currentMenu.put(player.getUniqueId(), type);
    }
    public static Type getCurrentMenu(Player player) {
        return currentMenu.getOrDefault(player.getUniqueId(), Type.CREATION_GUI);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        final Inventory inventory = e.getInventory();
        final Player player = (Player) e.getPlayer();

        if (inventory == GeneratorsGUI.inventory || inventory == WorldTypesGUI.inventory) {
            Bukkit.getScheduler().runTaskLater(MegaVerse.getInstance(), () -> {

                // Simple safety check
                if(getCurrentMenu(player) == Type.CREATION_GUI)
                    WorldCreationGUI.openMainMenu(player);

            }, 2);
        }
    }

    public static void deleteCache(Player player, Inventory mainMenu) {
        WorldCreationGUI.inventories.remove(mainMenu);
        WorldImportGUI.inventories.remove(mainMenu);

        WorldCreationGUI.verseCreatorHashMap.remove(player.getUniqueId());
        WorldImportGUI.verseCreatorHashMap.remove(player.getUniqueId());

        WorldImportGUI.importOptionsHashMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if(!isVerseGUI(e.getInventory()))
            return;

        final int slot = e.getSlot();
        final Player p = (Player) e.getWhoClicked();
        final Inventory inventory = e.getInventory();

        e.setCancelled(true);
        p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1);

        if(WorldCreationGUI.inventories.contains(inventory))
            WorldCreationGUI.onClick(slot, p, inventory, Type.CREATION_GUI);
        else if(WorldImportGUI.inventories.contains(inventory))
            WorldImportGUI.onClick(slot, p, inventory, Type.IMPORT_GUI);

        // Environment
        if(EnvironmentGUI.inventory == inventory)
            EnvironmentGUI.onClick(slot, p, inventory, getCurrentMenu(p));

        // Generators
        if(GeneratorsGUI.inventory == inventory)
            GeneratorsGUI.onClick(slot, p, inventory, getCurrentMenu(p));

        // World Types
        if(WorldTypesGUI.inventory == inventory)
            WorldTypesGUI.onClick(slot, p, inventory, getCurrentMenu(p));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if(isVerseGUI(e.getInventory()))
            e.setCancelled(true);
    }

    public static boolean isVerseGUI(final Inventory inventory) {
        return WorldCreationGUI.inventories.contains(inventory) || WorldImportGUI.inventories.contains(inventory)
                || inventory == GeneratorsGUI.inventory
                || inventory == EnvironmentGUI.inventory
                || inventory == WorldTypesGUI.inventory;
    }
    public static ItemStack simpleButton(Material m, String name, String... lore) {
        return simpleButton(m, name, 1, lore);
    }
    public static ItemStack simpleButton(Material m, String name, int count, String... lore) {
        ItemStack it = new ItemStack(m, count);
        ItemMeta im = it.getItemMeta();

        im.setDisplayName(Utils.GREEN_COLOR+"§l"+name);

        im.setLore(Arrays.stream(lore)
                .map(a -> Utils.c("&7"+a))
                .collect(Collectors.toList()));

        it.setItemMeta(im);
        return it;
    }

}
