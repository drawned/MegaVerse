package me.drawn.management;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseWorld;
import me.drawn.management.entities.VerseCreationOptions;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VerseWorldManager {

    private static final ArrayList<VerseWorld> verseWorlds = new ArrayList<>();

    public static List<VerseWorld> getAllVerseWorlds() {
        return verseWorlds;
    }

    public static Location getSafeSpawnLocation(World world) {
        return world.getHighestBlockAt(world.getSpawnLocation()).getRelative(BlockFace.UP).getLocation();
    }

    public static boolean unloadWorld(World w) {
        World backupWorld = Bukkit.getWorld("world");
        if(backupWorld == null) {
            List<World> worlds = Bukkit.getWorlds();
            worlds.remove(w);
            Collections.shuffle(worlds);
            backupWorld = worlds.get(0);
        }

        final Location safe = getSafeSpawnLocation(backupWorld);
        w.getPlayers().forEach(a -> a.teleport(safe));

        verseWorlds.remove(getVerseWorldByWorld(w));

        return Bukkit.unloadWorld(w, true);
    }

    public static VerseWorld getVerseWorldByName(String name) {
        return verseWorlds.stream()
                .filter(a -> name.equalsIgnoreCase(a.getName()))
                .findFirst().orElse(null);
    }
    public static VerseWorld getVerseWorldByWorld(World w) {
        return verseWorlds.stream()
                .filter(a -> a.getBukkitWorld().equals(w))
                .findFirst().orElse(null);
    }

    private static void importDefaultWorlds() {
        for(World w : Bukkit.getWorlds()) {
            registerNewWorld(w, null, true);
        }
    }

    public static void loadExistingWorlds() {
        importDefaultWorlds();

        for(File file : Objects.requireNonNull(MegaVerse.worldConfigsFolder.listFiles())) {
            final String worldName = file.getName().replace(".yml", "").trim();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            MegaVerse.log("&a| Loading world "+worldName);
            File verseWorldFile = new File(Bukkit.getWorldContainer(), worldName);

            if(!verseWorldFile.exists()) {
                MegaVerse.log("  &c| World "+worldName+" could not be loaded, the world folder does not exist.");
                MegaVerse.log("  &c| Manually delete the config file '"+worldName+".yml' inside world_configs folder in MegaVerse to stop seeing this message.");
                MegaVerse.empty();
                continue;
            }

            World w = null;

            if(!config.getBoolean("vanilla-world")) {
                MegaVerse.log("  &2| World seed is " + config.getString("seed", "Unknown"));
                MegaVerse.log("  &2| Generator is " + config.getString("generator", "Vanilla"));

                WorldCreator creator = new WorldCreator(worldName);

                if (config.contains("generator"))
                    creator.generator(config.getString("generator"));

                if (config.contains("environment"))
                    creator.environment(World.Environment.valueOf(config.getString("environment", "NORMAL")));

                creator.seed(config.getLong("seed"));

                WorldType type = WorldType.getByName(config.getString("world-type", "NORMAL"));
                if(type != null)
                    creator.type(type);

                creator.generateStructures(config.getBoolean("options.generate-structures"));

                creator.hardcore(config.getBoolean("options.hardcore"));

                w = Bukkit.createWorld(creator);
            } else {
                w = Bukkit.getWorld(worldName);
                MegaVerse.log("  &2| Detected as a vanilla default world.");
            }

            MegaVerse.log("  &2| &aImported and loaded world "+worldName+" successfully!");

            VerseWorld verseWorld = new VerseWorld(worldName, w, file, config);
            verseWorlds.add(verseWorld);

            MegaVerse.empty();
        }
    }

    private static void deleteWorldFileFromMegaverse(final String worldName) {
        File f = new File(MegaVerse.worldConfigsFolder, worldName+".yml");

        if(f.exists())
            f.delete();
    }

    public static boolean deleteWorld(final String worldName) {
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            return false;
        }

        deleteWorldFileFromMegaverse(worldName);
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        return deleteRecursively(worldFolder);
    }

    private static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!deleteRecursively(child)) return false;
            }
        }
        return file.delete();
    }

    public static String stringifyGenerator(ChunkGenerator generator) {
        return generator == null ? "VANILLA" : generator.toString();
    }

    private static VerseWorld registerNewWorld(World world, VerseCreationOptions creationOptions, boolean vanillaWorld) {
        File verseWorldFile = new File(MegaVerse.worldConfigsFolder, world.getName()+".yml");

        if(!verseWorldFile.exists()) {
            try {
                verseWorldFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(verseWorldFile);
            config.set("seed", String.valueOf(world.getSeed()));

            config.set("options.hardcore", world.isHardcore());

            if(creationOptions != null && creationOptions.hasChunkGenerator()) {
                config.set("generator", creationOptions.getGeneratorFullName());
            }

            config.set("world-type", world.getWorldType().name());
            config.set("environment", world.getEnvironment().toString());

            if (vanillaWorld)
                config.set("vanilla-world", true);

            config.set("options.difficulty", world.getDifficulty().name());

            config.set("flags.pvp", true);
            config.set("flags.allow-weather-change", true);

            try {
                config.save(verseWorldFile);
            } catch (IOException ex) {
                ex.fillInStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(verseWorldFile);

        VerseWorld verseWorld = new VerseWorld(world.getName(), world, verseWorldFile, config);
        verseWorlds.add(verseWorld);

        return verseWorld;
    }

    public static List<String> getLoadedWorldsNames() {
        return Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());
    }

    public static void createWorld(VerseCreationOptions options, WorldCreationCallback callback) {
        try {
            World w = Bukkit.createWorld(options.convertToWorldCreator());
            w.setKeepSpawnInMemory(false);

            MegaVerse.log("&aCreated new MegaVerse world "+options.worldName());

            VerseWorld verseWorld = registerNewWorld(w, options, false);
            callback.onWorldCreate(verseWorld);
        } catch (Exception ex) {
            callback.onError(ex);
        }
    }

    public interface WorldCreationCallback {

        public void onWorldCreate(VerseWorld world);

        public void onError(Exception exception);

    }

}
