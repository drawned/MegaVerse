package me.drawn;

import me.drawn.commands.MainCommand;
import me.drawn.commands.MainCommandTabComplete;
import me.drawn.gui.WorldCreationGUI;
import me.drawn.management.BuiltinGenerators;
import me.drawn.management.VerseGeneratorManager;
import me.drawn.management.VerseWorldManager;
import me.drawn.management.entities.VerseGenerator;
import me.drawn.utils.Metrics;
import me.drawn.utils.ServerVersion;
import me.drawn.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public final class MegaVerse extends JavaPlugin {

    public static MegaVerse getInstance() {
        return MegaVerse.getPlugin(MegaVerse.class);
    }

    public static File worldConfigsFolder;

    public static Logger l;

    @Override
    public void onEnable() {
        l = this.getLogger();
        worldConfigsFolder.mkdirs();

        ServerVersion.determineVersion();

        log("&2 _  _  ____  ___   __   _  _  ____  ____  ____  ____ ");
        log("&2( \\/ )(  __)/ __) / _\\ / )( \\(  __)(  _ \\/ ___)(  __)");
        log("&2/ \\/ \\ ) _)( (_ \\/    \\\\ \\/ / ) _)  )   /\\___ \\ ) _)");
        log("&2\\_)(_/(____)\\___/\\_/\\_/ \\__/ (____)(__\\_)(____/(____)");
        empty();

        log("&aEnabling MegaVerse v"+this.getDescription().getVersion());
        log("&aRunning on "+ServerVersion.serverVersion+" - MC "+ServerVersion.mcVersion);
        empty();

        log("&fImporting default world generators from other plugins, you can ignore any errors below.");
        VerseGeneratorManager.importGenericGenerators();
        empty();

        log("&fInitializing commands...");
        this.getCommand("megaverse").setExecutor(new MainCommand());

        if(ServerVersion.newerThan1_13)
            this.getCommand("megaverse").setTabCompleter(new MainCommandTabComplete());

        empty();

        log("&fRegistering events...");
        getServer().getPluginManager().registerEvents(new WorldCreationGUI(), this);
        empty();

        log("&fLoading worlds...");
        VerseWorldManager.loadExistingWorlds();
        empty();

        log("&fInitializing metrics... You can disable by disabling bStats in your plugins folder.");
        initializeMetrics();
        empty();
    }

    private static void initializeMetrics() {
        Metrics metrics = new Metrics(MegaVerse.getInstance(), 25747);
    }

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();

        worldConfigsFolder = new File(getDataFolder(), "world_configs");

        BuiltinGenerators.registerAll();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        VerseGenerator verseGenerator = VerseGeneratorManager.getVerseGeneratorById(id);
        if(verseGenerator != null) {
            return verseGenerator.getChunkGenerator();
        }
        return null;
    }

    public static void empty() {l.info(" ");}

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage("[MegaVerse] "+ Utils.c(message));
    }

}
