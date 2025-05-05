package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.noises.FastNoiseLite;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.block.Biome; // Import necessário se for usar BiomeProvider
import org.bukkit.generator.BiomeProvider; // Import necessário se for usar BiomeProvider

import java.util.Arrays; // Import necessário se for usar BiomeProvider
import java.util.List; // Import necessário se for usar BiomeProvider
import java.util.Random;

public class FarLandsGenerator extends VerseGenerator {

    public FarLandsGenerator() {
        super(MegaVerse.getInstance(), "farlands_generator", "Far Lands Generator", new BukkitFarLandsGenerator(), Material.ENDER_EYE);
    }

    public static final class BukkitFarLandsGenerator extends ChunkGenerator {

        private final FastNoiseLite noise;
        private final double frequency = 0.015;
        private final double densityThreshold = 0.0;
        private final int minIslandY = 40;
        private final int maxIslandY = 160;
        private final int dirtLayerDepth = 3;

        public BukkitFarLandsGenerator() {
            this.noise = new FastNoiseLite();
            this.noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2); // OpenSimplex2
            this.noise.SetFrequency((float) frequency);
            this.noise.SetFractalType(FastNoiseLite.FractalType.FBm); // FBm
            this.noise.SetFractalOctaves(4);
            this.noise.SetFractalLacunarity(2.0f);
            this.noise.SetFractalGain(0.5f);
        }

        @Override
        public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            for (int y = minIslandY; y <= maxIslandY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int worldX = chunkX * 16 + x;
                        int worldY = y;
                        int worldZ = chunkZ * 16 + z;

                        float noiseValue = noise.GetNoise(worldX, worldY, worldZ);

                        double verticalCenter = (minIslandY + maxIslandY) / 2.0;
                        double distanceFactor = Math.abs(worldY - verticalCenter) / (verticalCenter - minIslandY);
                        double verticalBias = 1.0 - distanceFactor * 0.8;
                        double effectiveDensity = noiseValue * verticalBias;

                        if (effectiveDensity > densityThreshold) {
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }
                    }
                }
            }
        }

        final Material topMaterial = Material.GRASS_BLOCK;
        final Material belowTopMaterial = Material.DIRT;

        @Override
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int highestY = -1;
                    for (int y = maxIslandY; y >= minIslandY; y--) {
                        if (chunkData.getType(x, y, z) != Material.AIR) {
                            highestY = y;
                            break;
                        }
                    }

                    if (highestY != -1) {
                        chunkData.setBlock(x, highestY, z, topMaterial);

                        for (int i = 1; i <= dirtLayerDepth; i++) {
                            int currentY = highestY - i;
                            if (currentY < minIslandY || chunkData.getType(x, currentY, z) == Material.AIR) {
                                break;
                            }
                            if (chunkData.getType(x, currentY, z) == Material.STONE) {
                                chunkData.setBlock(x, currentY, z, belowTopMaterial);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {}

        @Override
        public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
            return new BiomeProvider() {
                @Override
                public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                    return Biome.PLAINS;
                }

                @Override
                public List<Biome> getBiomes(WorldInfo worldInfo) {
                    return Arrays.asList(Biome.PLAINS);
                }
            };
        }

        @Override
        public boolean shouldGenerateNoise() {
            return true;
        }

        @Override
        public boolean shouldGenerateSurface() {
            return true;
        }

        @Override
        public boolean shouldGenerateBedrock() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return true;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return true;
        }

        @Override
        public boolean shouldGenerateStructures() {
            return true;
        }
    }
}