package me.drawn.management.generators;

import me.drawn.MegaVerse;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.noises.FastNoiseLite;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class AetherGenerator extends VerseGenerator {

    public AetherGenerator() {
        super(MegaVerse.getInstance(), "aether_generator", "Aether Generator", new BukkitAetherGenerator(), Material.ELYTRA);
    }

    public static final class BukkitAetherGenerator extends ChunkGenerator {

        private FastNoiseLite noise;
        private final int seaLevel = 50; // Nível mínimo para as ilhas começarem a aparecer
        private final int islandHeight = 80; // Altura média das ilhas
        private final double noiseThreshold = -0.2; // Limite do ruído para gerar blocos
        private final double frequency = 0.015; // Frequência do ruído para o tamanho das ilhas

        public BukkitAetherGenerator() {
            // Configuração básica do FastNoiseLite
            noise = new FastNoiseLite();
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2); // Um bom tipo de ruído para terreno
            noise.SetFractalType(FastNoiseLite.FractalType.FBm); // Adiciona detalhe às ilhas
            noise.SetFractalOctaves(4); // Número de camadas de ruído
            noise.SetFractalLacunarity(2.0f); // Aumento da frequência por oitava
            noise.SetFractalGain(0.5f); // Diminuição da amplitude por oitava
            noise.SetFrequency((float) this.frequency); // Frequência base do ruído
        }

        @Override
        public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
            ChunkData chunkData = createChunkData(world);

            int worldX = chunkX * 16;
            int worldZ = chunkZ * 16;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                        // Calcula as coordenadas globais
                        int globalX = worldX + x;
                        int globalZ = worldZ + z;

                        // Obtém o valor do ruído 3D
                        // Usamos a coordenada Y global para que a geração varie verticalmente
                        float noiseValue = noise.GetNoise(globalX, y, globalZ);

                        // Mapeia o ruído para a geração de ilhas
                        // A ilha começa a aparecer acima do seaLevel e a densidade aumenta com a altura
                        // Multiplicamos o noiseValue por um fator baseado na altura para concentrar as ilhas no topo
                        double density = noiseValue + (double) (y - seaLevel) / islandHeight;

                        if (density > noiseThreshold) {
                            // Define o bloco com base na altura dentro da "ilha"
                            if (y > seaLevel + islandHeight * 0.8) { // Camada superior (grama)
                                chunkData.setBlock(x, y, z, Material.GRASS_BLOCK);
                            } else if (y > seaLevel + islandHeight * 0.6) { // Camada intermediária (terra)
                                chunkData.setBlock(x, y, z, Material.DIRT);
                            } else { // Camada inferior (pedra)
                                chunkData.setBlock(x, y, z, Material.STONE);
                            }
                        } else if (y < seaLevel) {
                            // Opcional: preencher abaixo de um certo nível com ar para garantir o void
                            chunkData.setBlock(x, y, z, Material.AIR);
                        }
                    }
                }
            }

            return chunkData;
        }

        // Implemente outros métodos necessários da ChunkGenerator, como:
        // getDefaultPopulators, canSpawn, getDefaultSpawnLocation, etc.
        // Para um mundo Aether simples, muitos podem retornar listas vazias ou null.

        @Override
        public boolean shouldGenerateBedrock() {
            return false; // Não queremos bedrock em ilhas flutuantes
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false; // Opcional: desabilitar cavernas padrão
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return true; // Opcional: permitir decorações padrão (árvores, flores)
        }

        @Override
        public boolean shouldGenerateMobs() {
            return true; // Opcional: permitir spawn de mobs padrão
        }

        @Override
        public boolean shouldGenerateStructures() {
            return false; // Opcional: desabilitar estruturas padrão (vilas, masmorras)
        }

        @Override
        public @NotNull Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
            // Define um local de spawn fixo ou baseado na primeira ilha gerada
            // Implementação simples: spawn em um local fixo acima do void
            return new Location(world, 0, seaLevel + islandHeight + 10, 0);
        }
    }

}
