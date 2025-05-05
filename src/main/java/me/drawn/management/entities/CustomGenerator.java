package me.drawn.management.entities;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class CustomGenerator {

    private final Plugin plugin;

    public CustomGenerator(Plugin ownerPlugin) {
        this.plugin = ownerPlugin;
    }

    public ChunkGenerator getChunkGenerator() {return getChunkGenerator(null);}
    public ChunkGenerator getChunkGenerator(@Nullable String id) {
        return plugin.getDefaultWorldGenerator("", id);
    }

    public String getName() {
        return plugin.getDescription().getName();
    }

    public String getReadableName() {
        return plugin.getDescription().getName();
    }

    public String getNameWithId(String id) {
        return getName()+":"+id;
    }

    public Material getIcon() {
        return Material.LIGHT_GRAY_DYE;
    }

}
