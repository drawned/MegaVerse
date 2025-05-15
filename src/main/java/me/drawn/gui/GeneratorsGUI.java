package me.drawn.gui;

import me.drawn.gui.custom.WorldCreationGUI;
import me.drawn.management.VerseGeneratorManager;
import me.drawn.management.entities.CustomGenerator;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

import static me.drawn.gui.VerseGUI.simpleButton;

public class GeneratorsGUI {

    private static final HashMap<Integer, CustomGenerator> generatorHashMap = new HashMap<>();

    public static void onClick(final int slot, final Player player, final Inventory inv, VerseGUI.Type type) {
        if(type == VerseGUI.Type.CREATION_GUI) {
            if (!WorldCreationGUI.verseCreatorHashMap.containsKey(player.getUniqueId())) return;

            CustomGenerator gen = GeneratorsGUI.getGeneratorInSlot(slot);
            if (gen != null) {
                WorldCreationGUI.verseCreatorHashMap.get(player.getUniqueId()).chunkGenerator(gen);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
            }

            player.closeInventory();
            WorldCreationGUI.openMainMenu(player);
        }
    }

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

    @Nullable
    public static CustomGenerator getGeneratorInSlot(final int slot) {
        return generatorHashMap.getOrDefault(slot, null);
    }

}
