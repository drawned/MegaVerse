package me.drawn.management.entities;

import me.drawn.MegaVerse;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

public class VerseGenerator extends CustomGenerator {

    private final String id;
    private final String generatorName;
    private final Plugin ownerPlugin;

    private Material icon;

    private final ChunkGenerator chunkGenerator;

    public VerseGenerator(Plugin ownerPlugin, String id, String generatorName, ChunkGenerator chunkGeneratorClass, Material icon) {
        super(ownerPlugin);

        this.ownerPlugin = ownerPlugin;
        this.id = id;
        this.generatorName = generatorName;
        this.chunkGenerator = chunkGeneratorClass;
        this.icon = icon;
    }

    @Override
    public Material getIcon() {
        return icon == null ? Material.LIGHT_GRAY_DYE : icon;
    }

    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @Override
    public String getName() {
        return MegaVerse.getInstance().getName()+":"+getId();
    }
    @Override
    public String getNameWithId(String id) {
        return MegaVerse.getInstance().getName()+":"+getId();
    }

    @Override
    public String getReadableName() {
        return generatorName;
    }

    public String getId() {
        return id;
    }
}
