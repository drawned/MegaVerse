package me.drawn.management;

import me.drawn.MegaVerse;
import me.drawn.management.entities.CustomGenerator;
import me.drawn.management.entities.GenericGenerator;
import me.drawn.management.entities.VerseGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class VerseGeneratorManager {

    private static final ArrayList<VerseGenerator> verseGenerators = new ArrayList<>();
    private static final ArrayList<GenericGenerator> genericGenerators = new ArrayList<>();

    public static List<VerseGenerator> getAllVerseGenerators() {
        return verseGenerators;
    }
    public static List<GenericGenerator> getAllGenericGenerators() {
        return genericGenerators;
    }

    public static boolean hasCustomWorldGenerator(Plugin plugin) {
        try {
            Method method = plugin.getClass().getDeclaredMethod("getDefaultWorldGenerator", String.class, String.class);
            return method.getDeclaringClass() == plugin.getClass();
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static void importGenericGenerators() {
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if(!plugin.getName().equalsIgnoreCase("megaverse")
                    && hasCustomWorldGenerator(plugin)) {
                GenericGenerator genericGenerator = new GenericGenerator(plugin);
                genericGenerators.add(genericGenerator);
                MegaVerse.log("  &2| Imported new generic generator from "+plugin.getDescription().getName());
            }
        }
    }

    public static VerseGenerator getVerseGeneratorById(String id) {
        return verseGenerators.stream()
                .filter(a -> id.equalsIgnoreCase(a.getId()) || id.equalsIgnoreCase(a.getName()))
                .findFirst().orElse(null);
    }

    public static List<ChunkGenerator> getAllActiveGenerators() {
        return Bukkit.getWorlds().stream()
                .map(World::getGenerator)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Nullable
    public static VerseGenerator getVerseFromChunkGenerator(ChunkGenerator generator) {
        if(generator == null) return null;
        return getAllVerseGenerators().stream()
                .filter(gen -> generator.equals(gen.getChunkGenerator()))
                .findFirst().orElse(null);
    }

    @Nullable
    public static GenericGenerator getGenericFromChunkGenerator(ChunkGenerator generator) {
        if(generator == null) return null;
        return getAllGenericGenerators().stream()
                .filter(gen -> generator.equals(gen.getChunkGenerator()))
                .findFirst().orElse(null);
    }

    public static List<CustomGenerator> getAllGenerators() {
        Set<CustomGenerator> all = new LinkedHashSet<>();
        all.addAll(getAllVerseGenerators());
        all.addAll(getAllGenericGenerators());
        return new ArrayList<>(all);
    }

    public static void registerGenerator(VerseGenerator generator) {
        verseGenerators.add(generator);
    }

}
