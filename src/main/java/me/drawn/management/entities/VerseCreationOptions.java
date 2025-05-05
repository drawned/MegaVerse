package me.drawn.management.entities;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.Random;

public class VerseCreationOptions {

    private final String worldName;

    private CustomGenerator chunkGenerator;

    private String chunkGeneratorFullName;

    private long seed;
    private World.Environment environment;
    private WorldType type;
    private boolean hardcore;

    public VerseCreationOptions(String worldName) {
        this.worldName = worldName;
        this.type = WorldType.NORMAL;
        this.environment = World.Environment.NORMAL;
        this.seed = (new Random()).nextLong();
        this.hardcore = false;
    }

    public String worldName() {
        return worldName;
    }

    public boolean hasChunkGenerator() {
        return chunkGenerator != null && chunkGeneratorFullName != null;
    }

    public VerseCreationOptions chunkGenerator(CustomGenerator generator) {
        this.chunkGenerator = generator;
        this.chunkGeneratorFullName = generator.getName();
        return this;
    }
    public VerseCreationOptions chunkGenerator(CustomGenerator generator, String id) {
        this.chunkGenerator = generator;
        this.chunkGeneratorFullName = generator.getNameWithId(id);
        return this;
    }
    public CustomGenerator chunkGenerator() {
        return this.chunkGenerator;
    }
    public String getGeneratorFullName() {
        if(chunkGeneratorFullName == null) return "Vanilla";
        return this.chunkGeneratorFullName;
    }

    public VerseCreationOptions seed(long seed) {
        this.seed = seed;
        return this;
    }
    public long seed() {
        return this.seed;
    }

    public VerseCreationOptions type(WorldType type) {
        this.type = type;
        return this;
    }
    public WorldType type() {
        return this.type;
    }

    public VerseCreationOptions environment(World.Environment environment) {
        this.environment = environment;
        return this;
    }
    public World.Environment environment() {
        return this.environment;
    }

    public VerseCreationOptions hardcore(boolean b) {
        this.hardcore = b;
        return this;
    }
    public boolean hardcore() {
        return this.hardcore;
    }

    public WorldCreator convertToWorldCreator() {
        WorldCreator creator = new WorldCreator(worldName());

        if(hasChunkGenerator())
            creator.generator(getGeneratorFullName());

        creator.seed(seed);

        if(environment != null)
            creator.environment(environment);

        if(type != null)
            creator.type(type);

        creator.hardcore(hardcore);

        return creator;
    }
}
