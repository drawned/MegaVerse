package me.drawn.management.entities;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VerseWorld {

    private final String name;
    private final World bukkitWorld;

    private final YamlConfiguration configuration;
    private final File file;

    private final List<VerseFlag> flagList;

    public VerseWorld(String name, World world, File file, YamlConfiguration config) {
        this.name = name;
        this.bukkitWorld = world;

        this.file = file;
        this.configuration = config;

        this.flagList = new ArrayList<>();

        for(String s : config.getStringList("flags")) {
            flagList.add(new VerseFlag(s, config.getBoolean("flags."+s)));
        }
    }

    public boolean isFlagTrue(String flagName) {
        return this.flagList.stream()
                .anyMatch(flag -> flag.getName().equalsIgnoreCase(flagName) && flag.getValue());
    }

    public YamlConfiguration getConfiguration() {return this.configuration;}
    public File getFile() {return this.file;}
    public List<VerseFlag> getFlags() {return this.flagList;}

    public String getGenerator() {
        return configuration.getString("generator", "Vanilla");
    }

    public Difficulty getDifficulty() {
        return bukkitWorld.getDifficulty();
    }

    public long getSeed() {
        return bukkitWorld.getSeed();
    }

    public void teleport(Player player) {
        Location loc = bukkitWorld.getHighestBlockAt(bukkitWorld.getSpawnLocation()).getRelative(BlockFace.UP).getLocation();

        player.teleport(loc.add(0.5, 0, 0.5));
    }

    public String getName() {return name;}
    public World getBukkitWorld() {return bukkitWorld;}

}
