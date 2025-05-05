package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import org.bukkit.Material;

public class SmallIslandsGenerator extends VerseGenerator {

    public SmallIslandsGenerator() {
        super(MegaVerse.getInstance(), "small_islands_generator", "Small Islands Generator", new BigIslandsGenerator.BukkitIslandGenerator(0.008f), Material.TALL_GRASS);
    }
}
