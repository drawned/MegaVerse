package me.drawn.utils;

import me.drawn.MegaVerse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Utils {

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String getWarningSymbol() {
        return ServerVersion.newerThan1_16 ?
                net.md_5.bungee.api.ChatColor.of("#c74242")+"⚠ " : "§c⚠ ";
    }

    public static void formalPlayerWarning(CommandSender sender, String warning) {
        sender.sendMessage(Utils.getWarningSymbol()+Utils.c(warning));
    }

    public static void normalMessage(CommandSender sender, String message) {
        sender.sendMessage(Utils.getPrefix()+Utils.c(message));
    }

    public static void incorrectUsage(CommandSender sender, String correctUsage) {
        sender.sendMessage(Utils.getWarningSymbol()+Utils.c("Correct usage: "+correctUsage));
    }

    public static final String GREEN_HEX = "#a5fc42";
    public static final String GREEN_COLOR = ServerVersion.newerThan1_16 ?
            net.md_5.bungee.api.ChatColor.of(GREEN_HEX)+"" : "§a";

    public static String getPrefix() {
        return GREEN_COLOR+"\uD83C\uDF0D §lMegaVerse §7→ §f";
    }

    public interface ChatInputCallback {

        void onInput(String response);

        void onCancel();

    }

    public static void awaitChatInput(Player player, ChatInputCallback callback) {
        player.sendTitle(Utils.GREEN_COLOR+"Write in chat", "§7Type `cancel` to quit!", 5, 60, 20);

        player.closeInventory();

        Listener listener = new Listener() {
            @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
            public void onChat(AsyncPlayerChatEvent e) {
                Bukkit.getScheduler().runTask(MegaVerse.getInstance(), () -> {
                    if(!e.getPlayer().equals(player))
                        return;

                    final String msg = e.getMessage();

                    switch(msg.toLowerCase()) {
                        case "cancel":
                        case "quit":
                        case "leave":
                        case "back": {
                            callback.onCancel();

                            e.getPlayer().sendMessage(ChatColor.RED+"Cancelling chat input.");

                            AsyncPlayerChatEvent.getHandlerList().unregister(this);
                            return;
                        }
                    }

                    AsyncPlayerChatEvent.getHandlerList().unregister(this);
                    callback.onInput(c(msg));
                });
            }
        };
        Bukkit.getServer().getPluginManager().registerEvents(listener, MegaVerse.getInstance());
    }

}
