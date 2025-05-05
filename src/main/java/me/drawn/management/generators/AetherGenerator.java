package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.noises.FastNoiseLite;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AetherGenerator extends VerseGenerator {

    public AetherGenerator() {
        super(MegaVerse.getInstance(), "aether_generator", "Aether Generator", new BukkitAetherGenerator(), Material.ELYTRA);
    }

    public static class BukkitAetherGenerator extends ChunkGenerator {

        private final FastNoiseLite noise;
        private final int islandAmplitude = 7;
        private final int baseHeight = 100;
        private final float frequency = 0.009f;
        private final float threshold = 0.3f;

        public BukkitAetherGenerator() {
            this.noise = new FastNoiseLite();
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(frequency);
            noise.SetFractalOctaves(4);
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        }

        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);
            int startX = chunkX * 16;
            int startZ = chunkZ * 16;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = startX + x;
                    int worldZ = startZ + z;

                    float noiseValue = noise.GetNoise(worldX, worldZ);
                    if (noiseValue > threshold) {
                        int height = (int) (noiseValue * islandAmplitude + baseHeight);
                        chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
                        chunk.setBlock(x, height - 1, z, Material.DIRT);
                        for (int y = height - 2; y > height - 6; y--) {
                            chunk.setBlock(x, y, z, Material.STONE);
                        }
                    }

                    biome.setBiome(x, z, Biome.PLAINS);
                }
            }

            return chunk;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return true;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return true;
        }

    }

}
