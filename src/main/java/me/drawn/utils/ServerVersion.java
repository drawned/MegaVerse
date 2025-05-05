package me.drawn.utils;

import org.bukkit.Bukkit;

public class ServerVersion {

    public static String serverVersion = "";
    public static String mcVersion = "";

    public static boolean newerThan1_13 = false;
    public static boolean newerThan1_16 = false;
    public static boolean newerThan1_10 = false;

    public static void determineVersion() {

        final String version = Bukkit.getBukkitVersion();

        serverVersion = version;

        mcVersion = version.split("\\-")[0].trim();

        try {
            Class.forName("org.bukkit.entity.Dolphin");
            newerThan1_13 = true;
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("org.bukkit.entity.Piglin");
            newerThan1_16 = true;
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("org.bukkit.entity.PolarBear");
            newerThan1_10 = true;
        } catch (ClassNotFoundException ignored) {}
    }

    public static boolean runningOnPaper() {
        try {
            Class.forName("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
        } catch (ClassNotFoundException ex) {
            return Bukkit.getVersion().contains("paper");
        }
        return true;
    }

}
