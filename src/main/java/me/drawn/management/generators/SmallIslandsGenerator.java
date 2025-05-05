package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.noises.FastNoiseLite;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class SmallIslandsGenerator extends VerseGenerator {

    public SmallIslandsGenerator() {
        super(MegaVerse.getInstance(), "small_islands_generator", "Small Islands Generator", new BukkitSmallIslandGenerator(), Material.TALL_GRASS);
    }

    public static final class BukkitSmallIslandGenerator extends ChunkGenerator {
        private final FastNoiseLite noise;
        private final int seaLevel = 63;
        private final int oceanFloorLevel = 30;
        private final double islandThreshold = 0.25; // Levemente reduzido para ajudar a formar ilhas maiores com a nova frequência

        // --- AJUSTES ---
        private final double frequency = 0.008; // << Frequência MENOR para ilhas MAIORES e mais SUAVES

        private final int islandMaxHeightAboveSea = 25; // << Altura MÁXIMA das ilhas AUMENTADA
        private final int deepslateStartY = 0; // << Nível Y onde deepslate começa a substituir pedra

        public BukkitSmallIslandGenerator() {
            this.noise = new FastNoiseLite();
            this.noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
            this.noise.SetFrequency((float) frequency);

            // --- AJUSTES nos Fractais para Suavizar ---
            this.noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            this.noise.SetFractalOctaves(3);          // << MENOS oitavas para mais suavidade
            this.noise.SetFractalLacunarity(2.0f);
            this.noise.SetFractalGain(0.45f);       // << Ganho levemente REDUZIDO para suavizar detalhes
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
                        // Garante que o fundo do oceano não vá para Y negativo facilmente, a menos que seja profundo
                        terrainHeight = Math.max(terrainHeight, worldMinY); // Garante que não seja menor que o mínimo do mundo
                    } else {
                        float islandHeightFactor = (noiseValue - (float) islandThreshold) / (1.0f - (float) islandThreshold);
                        // A altura base da ilha agora começa um pouco mais baixo para permitir praias melhores
                        int baseIslandHeight = seaLevel - 2; // Começa a subir a partir daqui
                        terrainHeight = baseIslandHeight + (int) (islandHeightFactor * (islandMaxHeightAboveSea + (seaLevel - baseIslandHeight)));
                        // Garante que a altura máxima não seja excedida (embora o fator já deva cuidar disso)
                        terrainHeight = Math.min(terrainHeight, seaLevel + islandMaxHeightAboveSea);
                    }


                    // Preenche o chunk com pedra/deepslate até a altura calculada
                    for (int y = worldMinY; y <= terrainHeight; y++) {
                        // --- LÓGICA DO DEEPSLATE ---
                        Material blockMaterial = (y < deepslateStartY) ? Material.DEEPSLATE : Material.STONE; // << Troca para DEEPSLATE abaixo de Y=0
                        // Evita sobrescrever bedrock se o terreno for muito baixo
                        if(chunkData.getType(x, y, z) != Material.BEDROCK) {
                            chunkData.setBlock(x, y, z, blockMaterial);
                        }
                    }

                    // Preenche com água (se necessário) - lógica inalterada
                    if (terrainHeight < seaLevel) {
                        for (int y = terrainHeight + 1; y < seaLevel; y++) {
                            // Evita colocar água onde deveria haver bedrock (caso raro)
                            if(chunkData.getType(x, y, z) != Material.BEDROCK) {
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
                    // Otimização: Começa a busca pela superfície um pouco acima do nível do mar
                    int startY = Math.min(chunkData.getMaxHeight() - 1, seaLevel + islandMaxHeightAboveSea + 5);
                    for (int y = startY; y >= worldInfo.getMinHeight(); y--) {
                        Material blockType = chunkData.getType(x, y, z);
                        // Considera DEEPSLATE como sólido também
                        if (blockType != Material.AIR && blockType != Material.WATER) {
                            highestY = y;
                            break;
                        }
                    }

                    if (highestY == -1) continue;

                    Material currentMaterial = chunkData.getType(x, highestY, z);
                    // Não modifica bedrock
                    if(currentMaterial == Material.BEDROCK) continue;

                    Material surfaceBlock;
                    Material belowSurfaceBlock;

                    // Determina o material base (pedra ou deepslate) para decidir o que está abaixo
                    Material baseMaterial = (highestY < deepslateStartY) ? Material.DEEPSLATE : Material.STONE;

                    if (highestY < seaLevel - 1) {
                        // Fundo do oceano
                        surfaceBlock = Material.SAND; // << REMOVIDO o Gravel daqui
                        // Removida a lógica que adicionava Clay aleatoriamente também, para simplificar
                        belowSurfaceBlock = baseMaterial; // << Fundo do oceano agora tem base de STONE ou DEEPSLATE

                        chunkData.setBlock(x, highestY, z, surfaceBlock);
                        // Apenas garante que o bloco logo abaixo seja o material base (se for o caso)
                        // Não coloca camadas de terra/areia abaixo no fundo do oceano
                        if(highestY - 1 >= worldInfo.getMinHeight()) {
                            // Verifica se o bloco abaixo é o mesmo que o material base esperado
                            // Isso evita substituir cavernas ou outras features que podem ter sido geradas
                            if (chunkData.getType(x, highestY - 1, z) == baseMaterial) {
                                // Não precisa mudar, já é stone/deepslate
                            } else if (chunkData.getType(x, highestY -1, z) == Material.STONE && baseMaterial == Material.DEEPSLATE) {
                                // Corrige se a lógica do generateNoise deixou STONE pouco acima do limite de DEEPSLATE
                                chunkData.setBlock(x, highestY - 1, z, baseMaterial);
                            } else if (chunkData.getType(x, highestY - 1, z) == Material.DEEPSLATE && baseMaterial == Material.STONE){
                                // Corrige caso raro inverso
                                chunkData.setBlock(x, highestY - 1, z, baseMaterial);
                            }
                        }

                    } else if (highestY < seaLevel + 2) {
                        // Praia ou borda da ilha
                        surfaceBlock = Material.SAND;
                        belowSurfaceBlock = Material.SAND; // Areia sobre areia para praias

                        chunkData.setBlock(x, highestY, z, surfaceBlock);
                        // Coloca algumas camadas de areia/arenito abaixo
                        for(int i=1; i <= 3 && highestY - i >= worldInfo.getMinHeight(); ++i) {
                            Material below = chunkData.getType(x, highestY - i, z);
                            // Só substitui o material base (STONE ou DEEPSLATE)
                            if(below == Material.STONE || below == Material.DEEPSLATE) {
                                chunkData.setBlock(x, highestY -i, z, (random.nextInt(3) == 0 ? Material.SANDSTONE : Material.SAND)); // Mais areia que arenito
                            } else {
                                break; // Para se encontrar ar (caverna) ou outro material
                            }
                        }

                    } else {
                        // Ilha (acima do nível da praia)
                        surfaceBlock = Material.GRASS_BLOCK;
                        belowSurfaceBlock = Material.DIRT;
                        chunkData.setBlock(x, highestY, z, surfaceBlock);
                        // Coloca 3 camadas de terra abaixo da grama
                        for(int i=1; i <= 3 && highestY - i >= worldInfo.getMinHeight(); ++i) {
                            Material below = chunkData.getType(x, highestY - i, z);
                            // Só substitui o material base (STONE ou DEEPSLATE)
                            if(below == Material.STONE || below == Material.DEEPSLATE) {
                                chunkData.setBlock(x, highestY -i, z, belowSurfaceBlock);
                            } else {
                                break; // Para se encontrar ar (caverna) ou outro material
                            }
                        }
                    }
                }
            }
        }

        // generateBedrock inalterado, mas importante para 1.18+
        @Override
        public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            int minY = worldInfo.getMinHeight();
            // Garante que bedrock não sobrescreva deepslate/stone colocados pelo generateNoise
            // A lógica original já faz isso implicitamente, mas podemos ser explícitos.
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Camada sólida na base, apenas se for ar (ou outro bloco inesperado)
                    if(chunkData.getType(x, minY, z).isAir() || chunkData.getType(x, minY, z) == Material.STONE) { // Previne sobrescrever algo já existente se minY não for o fundo absoluto
                        chunkData.setBlock(x, minY, z, Material.BEDROCK);
                    }

                    // Adiciona bedrock aleatório acima, substituindo apenas STONE/DEEPSLATE
                    for (int y = minY + 1; y < minY + 5; y++) {
                        if (random.nextInt(5) >= y - minY) { // Chance diminui com a altura
                            Material currentMat = chunkData.getType(x, y, z);
                            if(currentMat == Material.STONE || currentMat == Material.DEEPSLATE) {
                                chunkData.setBlock(x, y, z, Material.BEDROCK);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean shouldGenerateStructures() {
            return true; // Mantenha false a menos que queira estruturas vanilla
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return true; // Mantenha false a menos que queira decoração vanilla (minérios, árvores vanilla, etc.)
        }

        @Override
        public boolean shouldGenerateMobs() {
            return true;
        }
    }

}
