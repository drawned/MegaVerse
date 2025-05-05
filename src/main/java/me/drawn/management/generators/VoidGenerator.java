package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class VoidGenerator extends VerseGenerator {

    public VoidGenerator() {
        super(MegaVerse.getInstance(), "void_generator", "Void Generator", new BukkitVoidGenerator(), Material.STRUCTURE_VOID);
    }

    public static final class  BukkitVoidGenerator extends ChunkGenerator {
        @NotNull
        @Override
        public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.BiomeGrid biome) {
            ChunkData data = createChunkData(world);

            if(x == 0 && z == 0)
                data.setBlock(0, 64, 0, Material.BEDROCK);

            return data;
        }

        @Override
        public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
            return new VoidBiomeProvider();
        }

        public static class VoidBiomeProvider extends BiomeProvider {
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return Biome.PLAINS;
            }

            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return List.of(Biome.PLAINS);
            }
        }
    }
}
