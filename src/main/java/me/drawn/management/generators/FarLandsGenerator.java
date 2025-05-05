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
        private final double frequency = 0.015; // Frequência para o tamanho/espaçamento das ilhas
        private final double densityThreshold = 0.0; // Valor de ruído acima do qual o bloco é sólido. Ajuste fino aqui! (Valores > 0 = menos blocos, < 0 = mais blocos)
        private final int minIslandY = 40;      // Altura mínima onde ilhas podem começar a aparecer
        private final int maxIslandY = 160;     // Altura máxima onde ilhas podem terminar
        private final int dirtLayerDepth = 3;   // Profundidade da camada de terra/grama no topo

        // Construtor - Inicializa o FastNoiseLite para 3D
        public BukkitFarLandsGenerator() {
            this.noise = new FastNoiseLite();
            this.noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2); // OpenSimplex2 é ótimo para 3D e geralmente rápido
            this.noise.SetFrequency((float) frequency);

            // Configurações Fractais para adicionar detalhes às ilhas
            this.noise.SetFractalType(FastNoiseLite.FractalType.FBm); // FBm é um bom padrão
            this.noise.SetFractalOctaves(4);          // Número de camadas de detalhe
            this.noise.SetFractalLacunarity(2.0f);   // Quão mais detalhadas ficam as camadas (frequência aumenta)
            this.noise.SetFractalGain(0.5f);        // Influência das camadas de detalhe (amplitude diminui)
            // Opcional: Pode usar SetFractalWeightedStrength para suavizar um pouco as features fractais
        }

        // Gera a estrutura base das ilhas usando ruído 3D
        @Override
        public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // Itera apenas na faixa de Y onde as ilhas podem existir para eficiência
            for (int y = minIslandY; y <= maxIslandY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int worldX = chunkX * 16 + x;
                        int worldY = y;
                        int worldZ = chunkZ * 16 + z;

                        // Calcula o valor do ruído 3D neste ponto
                        float noiseValue = noise.GetNoise(worldX, worldY, worldZ);

                        // Adiciona um fator de "peso vertical" (opcional, mas ajuda a concentrar as ilhas)
                        // Faz as ilhas serem mais prováveis/densas no meio da faixa Y
                        double verticalCenter = (minIslandY + maxIslandY) / 2.0;
                        double distanceFactor = Math.abs(worldY - verticalCenter) / (verticalCenter - minIslandY); // 0 no centro, 1 nas bordas
                        double verticalBias = 1.0 - distanceFactor * 0.8; // Reduz a densidade perto do min/max Y (ajuste o 0.8)
                        double effectiveDensity = noiseValue * verticalBias;

                        // Se o valor do ruído (ajustado) for maior que o limiar, coloca um bloco sólido
                        if (effectiveDensity > densityThreshold) {
                            // O material base das ilhas
                            // Pode adicionar variações aqui mais tarde (ex: Aetherstone, etc.)
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }
                        // Se não, permanece AR (o padrão do ChunkData)
                    }
                }
            }
            // Importante: NÃO preenchemos nada abaixo de minIslandY, deixando o void vazio.
        }

        // Adiciona a superfície (grama, terra) no topo das ilhas
        @Override
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            Material topMaterial = Material.GRASS_BLOCK; // Material do topo
            Material belowTopMaterial = Material.DIRT; // Material logo abaixo do topo

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int highestY = -1;
                    // Encontra o bloco sólido mais alto na coluna (dentro da faixa relevante)
                    for (int y = maxIslandY; y >= minIslandY; y--) {
                        if (chunkData.getType(x, y, z) != Material.AIR) {
                            highestY = y;
                            break;
                        }
                    }

                    // Se encontrou um topo de ilha
                    if (highestY != -1) {
                        // Coloca o bloco de topo (grama)
                        chunkData.setBlock(x, highestY, z, topMaterial);

                        // Coloca camadas de terra abaixo da grama
                        for (int i = 1; i <= dirtLayerDepth; i++) {
                            int currentY = highestY - i;
                            // Para se chegar abaixo da ilha ou encontrar ar (caso de bordas finas)
                            if (currentY < minIslandY || chunkData.getType(x, currentY, z) == Material.AIR) {
                                break;
                            }
                            // Só substitui o material base (STONE) por terra
                            if (chunkData.getType(x, currentY, z) == Material.STONE) {
                                chunkData.setBlock(x, currentY, z, belowTopMaterial);
                            }
                        }
                        // A parte de baixo da ilha permanece como STONE (ou o material base definido em generateNoise)
                    }
                }
            }
        }

        // ESSENCIAL: Impede a geração da camada de bedrock no fundo do mundo
        @Override
        public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // Não faz NADA aqui para manter o void vazio.
        }

        // Define o Bioma Padrão (importante para a cor do céu, etc.)
        @Override
        public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
            // Retorna um BiomeProvider que define tudo como um bioma específico.
            // PLAINS é neutro, mas THE_VOID pode ser mais apropriado tematicamente.
            // Pode ser necessário criar uma classe simples FixedBiomeProvider se não existir no seu Spigot core.
            // Exemplo usando um BiomeProvider inline simples:
            return new BiomeProvider() {
                @Override
                public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                    return Biome.PLAINS; // Ou Biome.THE_VOID se quiser o céu escuro
                }

                @Override
                public List<Biome> getBiomes(WorldInfo worldInfo) {
                    // Lista de biomas que podem aparecer neste mundo (apenas um neste caso)
                    return Arrays.asList(Biome.PLAINS); // Ou Biome.THE_VOID
                }
            };
            // Se o acima não funcionar, retorne null e defina o bioma via Multiverse ou outro plugin.
            // return null;
        }
        // --- Outras Configurações ---

        @Override
        public boolean shouldGenerateNoise() {
            return true; // Precisamos da fase de ruído
        }

        @Override
        public boolean shouldGenerateSurface() {
            return true; // Precisamos da fase de superfície para grama/terra
        }

        @Override
        public boolean shouldGenerateBedrock() {
            return false; // NÃO queremos bedrock (já tratado no override vazio, mas bom reforçar)
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false; // Desabilita cavernas vanilla (podem parecer estranhas em ilhas flutuantes)
        }

        @Override
        public boolean shouldGenerateDecorations() {
            // Defina como 'true' se quiser usar BlockPopulators para adicionar árvores, minérios, etc. nas ilhas.
            // Por padrão, desabilitado para um mundo mais limpo inicialmente.
            return true;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return true; // Permite o spawn natural de mobs nas ilhas
        }

        @Override
        public boolean shouldGenerateStructures() {
            // Definitivamente 'false' para evitar vilas, etc., flutuando de forma estranha.
            return false;
        }

        // Opcional: Você pode adicionar BlockPopulators aqui para decorar as ilhas
    /*
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        // Exemplo: return Arrays.asList(new TreePopulator(), new OrePopulator());
        return super.getDefaultPopulators(world); // Ou return new ArrayList<>();
    }
    */
    }
}