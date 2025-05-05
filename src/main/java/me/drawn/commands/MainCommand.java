package me.drawn.commands;

import me.drawn.MegaVerse;
import me.drawn.gui.WorldCreationGUI;
import me.drawn.management.VerseWorldManager;
import me.drawn.management.entities.VerseFlag;
import me.drawn.management.entities.VerseWorld;
import me.drawn.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor {
    public static final List<String> SUB_COMMANDS = Arrays.asList("create", "info", "flag", "list", "tp", "import", "unload", "delete");

    private static final List<String> confirmDelete = new ArrayList<>();

    public static void sendWorldActionsComponent(CommandSender s, World world) {
        final String worldName = world.getName();

        BaseComponent component = new TextComponent("⌂ "+world.getName());
        component.setColor(net.md_5.bungee.api.ChatColor.of(Utils.GREEN_HEX));

        BaseComponent separator = new TextComponent(" - ");
        separator.setColor(ChatColor.WHITE);

        component.addExtra(separator);

        BaseComponent infoAction = new TextComponent(" [ⓘ INFO]");
        infoAction.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/megaverse info "+worldName));
        infoAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to view world info")));
        infoAction.setColor(ChatColor.GOLD);

        BaseComponent tpAction = new TextComponent(" [\uD83C\uDF0D TP]");
        tpAction.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/megaverse tp "+worldName));
        tpAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to teleport to this world.")));
        tpAction.setColor(ChatColor.AQUA);

        BaseComponent unloadAction = new TextComponent(" [ⓧ UNLOAD]");
        unloadAction.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/megaverse unload "+worldName));
        unloadAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to unload this world." +
                "\n§7Unloading the world will not delete it," +
                "\n§7in order to §cpermanently §7delete, use '/megaverse delete' command.")));
        unloadAction.setColor(ChatColor.GRAY);

        //BaseComponent deleteAction = new TextComponent(" [ⓧ]");
        //deleteAction.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/megaverse delete "+worldName));
        //deleteAction.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to §cpermanently §7delete this world.")));
        //deleteAction.setColor(ChatColor.RED);

        component.addExtra(infoAction);
        component.addExtra(tpAction);
        component.addExtra(unloadAction);
        //component.addExtra(deleteAction);

        s.spigot().sendMessage(component);
    }

    public static String divider = "§7* §m                                   §r §7*";

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if(s instanceof Player) {
            Player p = (Player)s;
            p.playSound(p.getLocation(), "entity.villager.work_cartographer", 1, 1);
        }

        if(args.length == 0) {
            s.sendMessage(divider);
            s.sendMessage(Utils.getPrefix()+"Running MegaVerse v"+ MegaVerse.getInstance().getDescription().getVersion());
            s.sendMessage(divider);
            s.sendMessage(Utils.c(Utils.GREEN_COLOR+"/megaverse create <world name> &7- Allows you to create a new world.\n"
                    +Utils.GREEN_COLOR+"/megaverse tp <world> [player] &7- Teleports to the specified world.\n"
                    +Utils.GREEN_COLOR+"/megaverse info [world] &7- View detailed info about a world.\n"
                    +Utils.GREEN_COLOR+"/megaverse list &7- Shows a list of all existing worlds.\n"
                    +Utils.GREEN_COLOR+"/megaverse import <world_name> &7- Imports a new downloaded world inside your server root folder.\n"
                    +Utils.GREEN_COLOR+"/megaverse unload <world> &7- Unloads an active world from the server.\n"
                    +Utils.GREEN_COLOR+"/megaverse delete <world_name> &7- Deletes an unloaded world from the server."));
            s.sendMessage(divider);
            return true;
        }

        if(args[0].equalsIgnoreCase("import")) {
            if(noPermission(s, "megaverse.command.import"))
                return true;

            Utils.formalPlayerWarning(s, "This command is still under development. MegaVerse is growing quickly, so this command will be available soon!");

            return true;
        }

        if(args[0].equalsIgnoreCase("list")) {
            if(noPermission(s, "megaverse.command.list"))
                return true;

            int size = Bukkit.getWorlds().size();

            s.sendMessage(divider);
            s.sendMessage(Utils.c(Utils.getPrefix()+"There is "+Utils.GREEN_COLOR+size+" &fworlds loaded."));
            s.sendMessage(divider);

            for(World w : Bukkit.getWorlds()) {
                sendWorldActionsComponent(s, w);
            }

            s.sendMessage(divider);

            return true;
        }

        if(args[0].equalsIgnoreCase("create")) {
            if(noPermission(s, "megaverse.command.create"))
                return true;

            if(args.length == 1) {
                Utils.incorrectUsage(s, "/megaverse create <world_name>");
                return true;
            }

            final String worldName = args[1];

            if(Bukkit.getWorld(worldName) != null) {
                Utils.formalPlayerWarning(s, "A world with this name already exists and is loaded on the server.");
                return true;
            }

            if(s instanceof Player) {
                Player p = (Player) s;

                WorldCreationGUI.openSelectMenu(p, args[1]);

                return true;
            }

            return true;
        }

        if(args[0].equalsIgnoreCase("info")) {
            if(noPermission(s, "megaverse.command.info"))
                return true;

            World w = null;
            if(s instanceof Player) {
                Player p = (Player)s;
                w = p.getWorld();
            } else if(args.length == 1) {
                Utils.incorrectUsage(s, "/megaverse info [world name]");
                return true;
            }
            if(args.length >= 2) {
                w = Bukkit.getWorld(args[1]);
                if(w == null) {
                    Utils.formalPlayerWarning(s, "No world found with the name "+args[1]);
                    return true;
                }
            }

            final VerseWorld verseWorld = VerseWorldManager.getVerseWorldByWorld(w);
            final String isVerseWorld = verseWorld != null ? "&2Yes" : "&cNo";

            s.sendMessage(divider);
            s.sendMessage(Utils.c(Utils.GREEN_COLOR+"World Name: &f"+w.getName()
                    +"\n"+Utils.GREEN_COLOR+"World Type: &f"+w.getWorldType()
                    +"\n"+Utils.GREEN_COLOR+"World UID: &f"+w.getUID()
                    +"\n"+Utils.GREEN_COLOR+"Environment Type: &f"+w.getEnvironment()
                    +"\n"+Utils.GREEN_COLOR+"Seed: &f"+w.getSeed()
                    +"\n"+divider
                    +"\n"+Utils.GREEN_COLOR+"Is MegaVerse World: "+isVerseWorld
            ));

            if(verseWorld != null) {
                s.sendMessage(Utils.c(Utils.GREEN_COLOR+"World Generator: &f"+verseWorld.getGenerator()
                        +"\n"+Utils.GREEN_COLOR+"Difficulty: &f"+w.getDifficulty()
                ));

                for(VerseFlag flag : verseWorld.getFlags()) {
                    s.sendMessage(Utils.c(Utils.GREEN_COLOR+"Flag "+flag.getName()+": "+flag.getValue()));
                }
            }

            s.sendMessage(divider);
            return true;
        }

        if(args[0].equalsIgnoreCase("unload")) {
            if(noPermission(s, "megaverse.command.unload"))
                return true;

            if(args.length == 1) {
                Utils.incorrectUsage(s, "/megaverse unload <world_name>");
                return true;
            }

            World w = Bukkit.getWorld(args[1]);
            if(w == null) {
                Utils.formalPlayerWarning(s, "This world does not exist or is not loaded on the server.");
                return true;
            }

            Utils.normalMessage(s, "Saving chunks and unloading this world, please wait...");

            if(VerseWorldManager.unloadWorld(w))
                Utils.normalMessage(s, "World successfully unloaded from the server!");
            else
                Utils.formalPlayerWarning(s, "An unknown error occurred when trying to unload this world.");

            return true;
        }

        if(args[0].equalsIgnoreCase("delete")) {
            if(noPermission(s, "megaverse.command.delete"))
                return true;

            if(args.length == 1) {
                Utils.incorrectUsage(s, "/megaverse delete <world_name>");
                return true;
            }
            final String worldName = args[1];

            World w = Bukkit.getWorld(worldName);
            if(w != null) {
                Utils.formalPlayerWarning(s, "You need to unload this world first before trying to delete it.");
                return true;
            }

            if(!confirmDelete.contains(s.getName())) {
                Utils.formalPlayerWarning(s, "Please understand that deleting a world is not recoverable; confirm the action by running the command again.");
                confirmDelete.add(s.getName());
                return true;
            }

            if(VerseWorldManager.deleteWorld(worldName))
                Utils.normalMessage(s, "World "+worldName+" successfully deleted from the server!");
            else
                Utils.formalPlayerWarning(s, "An unknown error occurred when trying to delete this world.");

            confirmDelete.remove(s.getName());
            return true;
        }

        if(args[0].equalsIgnoreCase("tp")) {
            if(noPermission(s, "megaverse.command.tp"))
                return true;

            if(args.length == 1) {
                Utils.incorrectUsage(s, "/megaverse tp <world name> [player]");
                return true;
            }

            World w = Bukkit.getWorld(args[1]);
            if(w == null) {
                Utils.formalPlayerWarning(s, "This world does not exists or is not loaded.");
                return true;
            }

            Location location = VerseWorldManager.getSafeSpawnLocation(w);

            if(s instanceof Player) {
                Player p = (Player) s;

                p.teleport(location);

                return true;
            } else {
                if(args.length == 2) {
                    s.sendMessage(Utils.GREEN_COLOR+"/megaverse tp <world name> [player]");
                    return true;
                }

                Player player = Bukkit.getPlayer(args[2]);

                if(player == null) {
                    Utils.formalPlayerWarning(s, "This player does not exist or is not online.");
                    return true;
                }

                player.teleport(location);
            }

            return true;
        }

        return false;
    }

    private static boolean noPermission(CommandSender s, final String permission) {
        boolean perm = s.hasPermission(permission);

        if(!perm)
            Utils.formalPlayerWarning(s, "You need this permission: "+permission+" in order to use this command.");

        return !perm;
    }
}
