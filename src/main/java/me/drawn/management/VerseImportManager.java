package me.drawn.management;

import me.paulferlitz.IO.NBTReader;
import me.paulferlitz.NBTTags.Tag_Byte;
import me.paulferlitz.NBTTags.Tag_Compound;
import me.paulferlitz.NBTTags.Tag_Long;
import me.paulferlitz.NBTTags.Tag_String;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VerseImportManager {

    public static List<String> getValidImportableWorlds() {
        return Arrays.stream(Objects.requireNonNull(Bukkit.getWorldContainer().listFiles()))
                .filter(File::isDirectory)
                .filter(file -> {
                    File levelDat = new File(file, "level.dat");
                    return levelDat.exists() && !VerseWorldManager.getLoadedWorldsNames().contains(file.getName());
                })
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static class ImportOptions {
        private final File worldDirectory;
        private final File levelDat;

        private final String version;
        private final boolean hardcoreMode;
        private final boolean raining;
        private final Date lastPlayed;
        private final Difficulty difficulty;
        private final String worldType;
        private final boolean wasModded;

        private final boolean wasSet;

        public ImportOptions(@NotNull  File worldDirectory) throws IOException {
            this.worldDirectory = worldDirectory;
            this.levelDat = new File(worldDirectory, "level.dat");

            if(levelDat.exists()) {
                NBTReader nbtReader = new NBTReader(levelDat);

                Tag_Compound root = nbtReader.read();

                boolean wasModded = ((Tag_Byte) root.getTagByName("WasModded")).getData() == 1;
                boolean hardcore = ((Tag_Byte) root.getTagByName("hardcore")).getData() == 1;
                boolean raining = ((Tag_Byte) root.getTagByName("raining")).getData() == 1 || ((Tag_Byte) root.getTagByName("thundering")).getData() == 1;
                Tag_String version = (Tag_String)((Tag_Compound)root.getTagByName("Version")).getTagByName("Name");

                String worldType = "Unknown";
                try {
                    Tag_Compound dimensions = (Tag_Compound) ((Tag_Compound) root.getTagByName("WorldGenSettings")).getTagByName("dimensions");
                    Tag_Compound overWorldGenerator = (Tag_Compound) ((Tag_Compound) dimensions.getTagByName("minecraft:overworld")).getTagByName("generator");
                    worldType = ((Tag_String) overWorldGenerator.getTagByName("type")).getData();
                } catch (Exception ignored) {}

                Difficulty difficulty = Difficulty.getByValue(((Tag_Byte) root.getTagByName("Difficulty")).getData());

                long lastPlayed = ((Tag_Long) root.getTagByName("LastPlayed")).getData();

                this.version = version.getData();
                this.hardcoreMode = hardcore;
                this.raining = raining;
                this.wasModded = wasModded;
                this.lastPlayed = new Date(lastPlayed);
                this.wasSet = true;
                this.difficulty = difficulty;
                this.worldType = worldType;
            } else {
                this.version = null;
                this.hardcoreMode = false;
                this.raining = false;
                this.lastPlayed = null;
                this.wasSet = false;
                this.wasModded = false;
                this.difficulty = null;
                this.worldType = null;
            }
        }

        public boolean isHardcoreMode() {
            return hardcoreMode;
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        public boolean wasModded() {
            return wasModded;
        }

        public String getVersion() {
            return version;
        }

        public String getWorldType() {
            return worldType;
        }

        public boolean isRaining() {
            return raining;
        }

        public Date getLastPlayed() {
            return lastPlayed;
        }

        public boolean wasSet() {
            return wasSet;
        }

        public File getLevelDat() {
            return levelDat;
        }
        public File getWorldDirectory() {
            return worldDirectory;
        }
    }
}
