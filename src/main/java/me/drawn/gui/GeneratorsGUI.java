package me.drawn.gui;

import me.drawn.management.VerseGeneratorManager;
import me.drawn.management.entities.CustomGenerator;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

import static me.drawn.utils.MenuUtils.simpleButton;

public class GeneratorsGUI {

    private static HashMap<Integer, CustomGenerator> generatorHashMap = new HashMap<>();

    public static final Inventory inventory = generateInventory();
    private static Inventory generateInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "All World Generators");

        List<CustomGenerator> generators = VerseGeneratorManager.getAllGenerators();

        int genericList = 18;
        int verseList = 0;
        for (CustomGenerator generator : generators) {
            Material icon = generator.getIcon();
            String readableName = generator.getReadableName();
            String ownerPlugin = generator.getName();

            int i = genericList;
            if(generator instanceof VerseGenerator) {
                readableName = Utils.GREEN_COLOR+"§l"+generator.getReadableName();

                i = verseList;
                verseList++;
                genericList--;
            }

            generatorHashMap.put(i, generator);
            inv.setItem(i, simpleButton(icon, "§f"+readableName, Utils.GREEN_COLOR+"From Plugin: &f"+ownerPlugin, " ", "&eClick to select this world generator."));

            genericList++;
        }

        final ItemStack dividerIt = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta im = dividerIt.getItemMeta();
        im.setDisplayName("");
        dividerIt.setItemMeta(im);

        for(int divider = 9; divider < 18; divider++) {
            inv.setItem(divider, dividerIt);
        }

        return inv;
    }

    private static ItemStack getDividerItem() {
        ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName("");
        itemStack.setItemMeta(im);
        return itemStack;
    }

    @Nullable
    public static CustomGenerator getGeneratorInSlot(final int slot) {
        return generatorHashMap.getOrDefault(slot, null);
    }

}
