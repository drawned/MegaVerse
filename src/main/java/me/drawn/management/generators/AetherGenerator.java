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
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AetherGenerator extends VerseGenerator {

    public AetherGenerator() {
        super(MegaVerse.getInstance(), "aether_generator", "Aether Generator", new BukkitAetherGenerator(6469L), Material.ELYTRA);
    }

    public static class BukkitAetherGenerator extends ChunkGenerator {

        private final FastNoiseLite placementNoise;
        private final FastNoiseLite islandSpecificParamNoise;
        private final FastNoiseLite shapeNoise;
        private final long worldSeed;

        private static final float PLACEMENT_FREQUENCY = 0.007f;
        private static final float ISLAND_PARAM_FREQUENCY = 0.1f;
        private static final float SHAPE_NOISE_FREQUENCY = 0.035f;

        private static final float ISLAND_PLACEMENT_THRESHOLD = 0.50f;
        private static final float ISLAND_MAX_RADIUS_BASE = 30.0f;
        private static final float ISLAND_MAX_RADIUS_VARIATION = 45.0f;
        private static final float ISLAND_HEIGHT_BASE = 20.0f;
        private static final float ISLAND_HEIGHT_VARIATION = 60.0f;
        private static final int ISLAND_MEAN_TOP_Y_LEVEL = 60;
        private static final int ISLAND_TOP_Y_LEVEL_VARIATION = 30;

        private static final int DIRT_LAYER_THICKNESS = 4;

        private static final int ISLAND_GRID_CELL_SIZE = 128;
        private static final int ISLAND_SEARCH_GRID_RADIUS = 1;

        public BukkitAetherGenerator(long seed) {
            this.worldSeed = seed;

            this.placementNoise = new FastNoiseLite((int) (seed));
            this.placementNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            this.placementNoise.SetFrequency(PLACEMENT_FREQUENCY);

            this.islandSpecificParamNoise = new FastNoiseLite((int) (seed + 1));
            this.islandSpecificParamNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            this.islandSpecificParamNoise.SetFrequency(ISLAND_PARAM_FREQUENCY);

            this.shapeNoise = new FastNoiseLite((int) (seed + 2));
            this.shapeNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
            this.shapeNoise.SetFrequency(SHAPE_NOISE_FREQUENCY);
        }

        @Override
        public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
            ChunkData chunkData = createChunkData(world);

            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight();

            int centerChunkOriginX = chunkX * 16 + 8;
            int centerChunkOriginZ = chunkZ * 16 + 8;

            int currentChunkGridX = Math.floorDiv(centerChunkOriginX, ISLAND_GRID_CELL_SIZE);
            int currentChunkGridZ = Math.floorDiv(centerChunkOriginZ, ISLAND_GRID_CELL_SIZE);

            for (int gridOffsetX = -ISLAND_SEARCH_GRID_RADIUS; gridOffsetX <= ISLAND_SEARCH_GRID_RADIUS; gridOffsetX++) {
                for (int gridOffsetZ = -ISLAND_SEARCH_GRID_RADIUS; gridOffsetZ <= ISLAND_SEARCH_GRID_RADIUS; gridOffsetZ++) {
                    int islandGridX = currentChunkGridX + gridOffsetX;
                    int islandGridZ = currentChunkGridZ + gridOffsetZ;

                    long islandCellSeed = this.worldSeed ^ (islandGridX * 73856093L) ^ (islandGridZ * 19349663L);
                    this.islandSpecificParamNoise.SetSeed((int)islandCellSeed);

                    float placementValue = this.placementNoise.GetNoise(islandGridX * ISLAND_GRID_CELL_SIZE, islandGridZ * ISLAND_GRID_CELL_SIZE);

                    if (placementValue > ISLAND_PLACEMENT_THRESHOLD) {
                        float offsetXRatio = (this.islandSpecificParamNoise.GetNoise(10.0f, 10.0f) * 0.5f) + 0.5f;
                        float offsetZRatio = (this.islandSpecificParamNoise.GetNoise(20.0f, 20.0f) * 0.5f) + 0.5f;
                        float islandCenterX = (islandGridX + offsetXRatio) * ISLAND_GRID_CELL_SIZE;
                        float islandCenterZ = (islandGridZ + offsetZRatio) * ISLAND_GRID_CELL_SIZE;

                        float heightNoiseVal = (this.islandSpecificParamNoise.GetNoise(30.0f, 30.0f) * 0.5f) + 0.5f;
                        float radiusNoiseVal = (this.islandSpecificParamNoise.GetNoise(40.0f, 40.0f) * 0.5f) + 0.5f;
                        float yOffsetNoiseVal = this.islandSpecificParamNoise.GetNoise(50.0f, 50.0f);

                        float islandHeight = ISLAND_HEIGHT_BASE + heightNoiseVal * ISLAND_HEIGHT_VARIATION;
                        float islandMaxRadius = ISLAND_MAX_RADIUS_BASE + radiusNoiseVal * ISLAND_MAX_RADIUS_VARIATION;

                        float islandTopSurfaceY = ISLAND_MEAN_TOP_Y_LEVEL + yOffsetNoiseVal * ISLAND_TOP_Y_LEVEL_VARIATION;
                        float islandBottomTipY = islandTopSurfaceY - islandHeight;

                        if (islandHeight <= 0 || islandMaxRadius <=0) continue;

                        for (int localX = 0; localX < 16; localX++) {
                            for (int localZ = 0; localZ < 16; localZ++) {
                                int worldX = chunkX * 16 + localX;
                                int worldZ = chunkZ * 16 + localZ;

                                double distSqToIslandAxis = (worldX - islandCenterX) * (worldX - islandCenterX) + (worldZ - islandCenterZ) * (worldZ - islandCenterZ);

                                for (int worldY = minY; worldY < maxY; worldY++) {
                                    if (worldY >= islandBottomTipY && worldY < islandTopSurfaceY) {
                                        double heightRatioFromTip = (worldY - islandBottomTipY) / islandHeight;

                                        if (heightRatioFromTip < 0) heightRatioFromTip = 0;
                                        if (heightRatioFromTip > 1) heightRatioFromTip = 1;

                                        double currentTheoreticalRadius = islandMaxRadius * Math.pow(heightRatioFromTip, 0.65);

                                        if (currentTheoreticalRadius <= 0) continue;

                                        float surfacePerturbationAmount = 3.5f;
                                        float surfaceNoiseVal = this.shapeNoise.GetNoise(worldX, worldY, worldZ);
                                        double perturbedRadius = currentTheoreticalRadius + surfaceNoiseVal * surfacePerturbationAmount;

                                        if (perturbedRadius <= 0) continue;

                                        if (distSqToIslandAxis < perturbedRadius * perturbedRadius) {
                                            Material blockMaterial;

                                            float actualTopSurfaceForMaterial = islandTopSurfaceY + this.shapeNoise.GetNoise(worldX * 0.25f, worldZ * 0.25f) * 2.5f;

                                            if (worldY >= actualTopSurfaceForMaterial - 1 && worldY < islandTopSurfaceY + 5) { // Cap grass generation near the top
                                                blockMaterial = Material.GRASS_BLOCK;
                                            } else if (worldY >= actualTopSurfaceForMaterial - DIRT_LAYER_THICKNESS && worldY < islandTopSurfaceY + 5) {
                                                if(random.nextInt(4) == 3)
                                                    blockMaterial = Material.COARSE_DIRT;
                                                else
                                                    blockMaterial = Material.DIRT;
                                            } else {
                                                blockMaterial = Material.STONE;
                                            }

                                            // Ensure block is placed within chunkdata vertical limits
                                            if (worldY >= chunkData.getMinHeight() && worldY < chunkData.getMaxHeight()) {
                                                chunkData.setBlock(localX, worldY, localZ, blockMaterial);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int biomeMinY_cell = Math.floorDiv(minY, 4);
            int biomeMaxY_cell = Math.floorDiv(maxY, 4);

            for (int bioX = 0; bioX < 4; bioX++) {
                for (int bioZ = 0; bioZ < 4; bioZ++) {
                    for (int bioY = biomeMinY_cell; bioY < biomeMaxY_cell; bioY++) {
                        if (chunkData.getMinHeight() <= bioY*4 && bioY*4 < chunkData.getMaxHeight()){
                            biome.setBiome(bioX, bioY - Math.floorDiv(chunkData.getMinHeight(),4) , bioZ, Biome.THE_VOID);
                        }
                    }
                }
            }

            return chunkData;
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