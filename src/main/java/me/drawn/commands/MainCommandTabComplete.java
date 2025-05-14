package me.drawn.commands;

import me.drawn.management.VerseImportManager;
import me.drawn.management.VerseWorldManager;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;

public class MainCommandTabComplete implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
        if(s instanceof Player) {
            // no lambda because java 11 brr
            Player p = (Player) s;
            p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1);
        }

        if(args.length == 1) {
            return MainCommand.SUB_COMMANDS;
        }

        if(args.length == 2) {
            final String subCommand = args[0].toLowerCase();

            if(subCommand.equals("create") && s.hasPermission("megaverse.command.create"))
                return List.of("<world_name>");

            if(subCommand.equals("info") || subCommand.equals("unload") || subCommand.equals("delete") || subCommand.equals("tp")) {
                if(s.hasPermission("megaverse.command."+subCommand))
                    return VerseWorldManager.getLoadedWorldsNames();
            }

            if(subCommand.equals("import") && s.hasPermission("megaverse.command.import")) {
                return VerseImportManager.getValidImportableWorlds();
            }
        }

        return List.of();
    }
}