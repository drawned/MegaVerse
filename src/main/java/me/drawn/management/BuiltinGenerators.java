package me.drawn.management;

import me.drawn.management.generators.*;

public class BuiltinGenerators {
    public static void registerAll() {
        SmallIslandsGenerator smallIslandGenerator = new SmallIslandsGenerator();
        BigIslandsGenerator bigIslandGenerator = new BigIslandsGenerator();
        VoidGenerator voidGenerator = new VoidGenerator();
        FarLandsGenerator farLandsGenerator = new FarLandsGenerator();
        AetherGenerator aetherGenerator = new AetherGenerator();

        VerseGeneratorManager.registerGenerator(smallIslandGenerator);
        VerseGeneratorManager.registerGenerator(bigIslandGenerator);
        VerseGeneratorManager.registerGenerator(voidGenerator);
        VerseGeneratorManager.registerGenerator(farLandsGenerator);
        VerseGeneratorManager.registerGenerator(aetherGenerator);
    }
}
