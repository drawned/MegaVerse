package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.noises.FastNoiseLite;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class BigIslandsGenerator extends VerseGenerator {

    public BigIslandsGenerator() {
        super(MegaVerse.getInstance(), "big_islands_generator", "Big Islands Generator", new BukkitIslandGenerator(0.0035f), Material.GRASS_BLOCK);
    }

    public static final class BukkitIslandGenerator extends ChunkGenerator {
        private final FastNoiseLite noise;
        private final int seaLevel = 63;
        private final int oceanFloorLevel = 30;
        private final double islandThreshold = 0.25;

        private final int islandMaxHeightAboveSea = 25;
        private final int deepslateStartY = 0;

        public BukkitIslandGenerator(float frequency) {
            this.noise = new FastNoiseLite();
            this.noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
            this.noise.SetFrequency(frequency);
            this.noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            this.noise.SetFractalOctaves(3);
            this.noise.SetFractalLacunarity(2.0f);
            this.noise.SetFractalGain(0.45f);
        }

        @Override
        public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            int worldMinY = worldInfo.getMinHeight();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = chunkX * 16 + x;
                    int worldZ = chunkZ * 16 + z;

                    float noiseValue = noise.GetNoise(worldX, worldZ);
                    int terrainHeight;

                    if (noiseValue < islandThreshold) {
                        float oceanDepthFactor = (noiseValue + 1.0f) / (float) (islandThreshold + 1.0f);
                        terrainHeight = oceanFloorLevel + (int) (oceanDepthFactor * (seaLevel - 1 - oceanFloorLevel));
                        terrainHeight = Math.max(oceanFloorLevel, terrainHeight);
                        terrainHeight = Math.max(terrainHeight, worldMinY); // Garante que não seja menor que o mínimo do mundo
                    } else {
                        float islandHeightFactor = (noiseValue - (float) islandThreshold) / (1.0f - (float) islandThreshold);
                        int baseIslandHeight = seaLevel - 2; // Começa a subir a partir daqui
                        terrainHeight = baseIslandHeight + (int) (islandHeightFactor * (islandMaxHeightAboveSea + (seaLevel - baseIslandHeight)));
                        terrainHeight = Math.min(terrainHeight, seaLevel + islandMaxHeightAboveSea);
                    }

                    for (int y = worldMinY; y <= terrainHeight; y++) {
                        Material blockMaterial = (y < deepslateStartY) ? Material.DEEPSLATE : Material.STONE;
                        if (chunkData.getType(x, y, z) != Material.BEDROCK) {
                            chunkData.setBlock(x, y, z, blockMaterial);
                        }
                    }

                    if (terrainHeight < seaLevel) {
                        for (int y = terrainHeight + 1; y < seaLevel; y++) {
                            if (chunkData.getType(x, y, z) != Material.BEDROCK) {
                                chunkData.setBlock(x, y, z, Material.WATER);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int highestY = -1;
                    int startY = Math.min(chunkData.getMaxHeight() - 1, seaLevel + islandMaxHeightAboveSea + 5);
                    for (int y = startY; y >= worldInfo.getMinHeight(); y--) {
                        Material blockType = chunkData.getType(x, y, z);
                        if (blockType != Material.AIR && blockType != Material.WATER) {
                            highestY = y;
                            break;
                        }
                    }

                    if (highestY == -1) continue;

                    Material currentMaterial = chunkData.getType(x, highestY, z);
                    if (currentMaterial == Material.BEDROCK) continue;

                    Material surfaceBlock;
                    Material belowSurfaceBlock;

                    Material baseMaterial = (highestY < deepslateStartY) ? Material.DEEPSLATE : Material.STONE;

                    if (highestY < seaLevel - 1) {
                        surfaceBlock = Material.SAND;

                        chunkData.setBlock(x, highestY, z, surfaceBlock);
                        if (highestY - 1 >= worldInfo.getMinHeight()) {
                            if (chunkData.getType(x, highestY - 1, z) == baseMaterial) {
                                // already stone
                            } else if (chunkData.getType(x, highestY - 1, z) == Material.STONE && baseMaterial == Material.DEEPSLATE) {
                                chunkData.setBlock(x, highestY - 1, z, baseMaterial);
                            } else if (chunkData.getType(x, highestY - 1, z) == Material.DEEPSLATE && baseMaterial == Material.STONE) {
                                chunkData.setBlock(x, highestY - 1, z, baseMaterial);
                            }
                        }

                    } else if (highestY < seaLevel + 2) {
                        surfaceBlock = Material.SAND;

                        chunkData.setBlock(x, highestY, z, surfaceBlock);
                        for (int i = 1; i <= 3 && highestY - i >= worldInfo.getMinHeight(); ++i) {
                            Material below = chunkData.getType(x, highestY - i, z);
                            if (below == Material.STONE || below == Material.DEEPSLATE) {
                                chunkData.setBlock(x, highestY - i, z, (random.nextInt(3) == 0 ? Material.SANDSTONE : Material.SAND));
                            } else {
                                break;
                            }
                        }

                    } else {
                        surfaceBlock = Material.GRASS_BLOCK;
                        belowSurfaceBlock = Material.DIRT;
                        chunkData.setBlock(x, highestY, z, surfaceBlock);
                        for (int i = 1; i <= 3 && highestY - i >= worldInfo.getMinHeight(); ++i) {
                            Material below = chunkData.getType(x, highestY - i, z);
                            if (below == Material.STONE || below == Material.DEEPSLATE) {
                                chunkData.setBlock(x, highestY - i, z, belowSurfaceBlock);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            int minY = worldInfo.getMinHeight();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (chunkData.getType(x, minY, z).isAir() || chunkData.getType(x, minY, z) == Material.STONE) {
                        chunkData.setBlock(x, minY, z, Material.BEDROCK);
                    }
                    for (int y = minY + 1; y < minY + 5; y++) {
                        if (random.nextInt(5) >= y - minY) {
                            Material currentMat = chunkData.getType(x, y, z);
                            if (currentMat == Material.STONE || currentMat == Material.DEEPSLATE) {
                                chunkData.setBlock(x, y, z, Material.BEDROCK);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean shouldGenerateStructures() {
            return true;
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
