package eu.adiih.buildffa.commands;

import eu.adiih.buildffa.FFAPlugin;
import eu.adiih.buildffa.objects.FFAMap;
import eu.adiih.buildffa.utils.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildFFACommand implements CommandExecutor, TabCompleter {

    private final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!sender.hasPermission("buildffa.admin")) return false;
        if(!(sender instanceof Player)) {
            if(args.length == 4 && args[0].equalsIgnoreCase("give")) {
                giveItem(sender, args);
            }
            if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfigurationFiles();
                sender.sendMessage(plugin.getTranslation("reload"));
            }
            return false;
        }
        Player player = (Player) sender;
        if(args.length == 0) {
            for(String line : plugin.getConfig().getStringList("messages.helpMenu")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', line.replace("%command%", alias)));
            }
        }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfigurationFiles();
                player.sendMessage(plugin.getTranslation("reload"));
            }
            if(args[0].equalsIgnoreCase("setup")) {
                for(String line : plugin.getConfig().getStringList("messages.setupHelp")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', line.replace("%command%", alias)));
                }
            }
            if(args[0].equalsIgnoreCase("skipmap") || args[0].equalsIgnoreCase("nextmap")) {
                plugin.setMapExpirySeconds(0);
            }
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("setup")) {
                if(args[1].equalsIgnoreCase("savedefaultkit")) {
                    InventoryUtils.saveInventoryContents(player.getInventory().getContents());
                    player.sendMessage(plugin.getTranslation("setup.defaultKitSaved"));
                }
            }
            if(args[0].equalsIgnoreCase("setmap")) {
                FFAMap desiredMap = plugin.getMapByName(args[1]);
                plugin.getGameNode().changeMap(desiredMap);
                plugin.getServer().broadcastMessage(plugin.getTranslation("mapChanged").replace("%map%", desiredMap.getDisplayName()));
            }
        }
        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("setup")) {
                if(args[1].equalsIgnoreCase("create")) {
                    World world = plugin.getServer().getWorld(args[2]);
                    if(world == null) {
                        player.sendMessage(plugin.getTranslation("setup.createError"));
                        return false;
                    }
                    if(plugin.getStorageConfig().isSet("maps." + args[2])) {
                        player.sendMessage(plugin.getTranslation("setup.alreadyExisting"));
                        return false;
                    }
                    player.teleport(world.getSpawnLocation());
                    plugin.createNewMap(world);
                    player.sendMessage(plugin.getTranslation("setup.createSuccess").replace("%world%", world.getName()));
                }
                if(args[1].equalsIgnoreCase("delete")) {
                    if(!plugin.getStorageConfig().isSet("maps." + args[2])) {
                        player.sendMessage(plugin.getTranslation("setup.noSuchMap"));
                        return false;
                    }
                    if(plugin.getGameNode().getCurrentMap().getWorld().getName().equalsIgnoreCase(args[2])) {
                        player.sendMessage(plugin.getTranslation("setup.deleteError"));
                        return false;
                    }
                    plugin.getStorageConfig().set("maps." + args[2], null);
                    plugin.saveStorage();
                    player.sendMessage(plugin.getTranslation("setup.deleted").replace("%map%", args[2]));
                }
                if(args[1].equalsIgnoreCase("setspawn")) {
                    if(!plugin.getStorageConfig().isSet("maps." + args[2])) {
                        player.sendMessage(plugin.getTranslation("setup.noSuchMap"));
                        return false;
                    }
                    plugin.getStorageConfig().set("maps." + args[2] + ".spawnPoint", player.getLocation());
                    plugin.saveStorage();
                    player.sendMessage(plugin.getTranslation("setup.spawnSet").replace("%map%", args[2]));
                }
            }
        }
        if(args.length == 4) {
            if(args[0].equalsIgnoreCase("give")) {
                giveItem(player, args);
            }
            if(args[0].equalsIgnoreCase("setup")) {
                if(args[1].equalsIgnoreCase("setdisplayname")) {
                    if(!plugin.getStorageConfig().isSet("maps." + args[2])) {
                        player.sendMessage(plugin.getTranslation("setup.noSuchMap"));
                        return false;
                    }
                    plugin.getStorageConfig().set("maps." + args[2] + ".displayName", ChatColor.translateAlternateColorCodes('&', args[3]));
                    plugin.saveStorage();
                    player.sendMessage(plugin.getTranslation("setup.displayNameSet").replace("%map%", args[2]).replace("%displayName%", ChatColor.translateAlternateColorCodes('&', args[3])));
                }
                if(args[1].equalsIgnoreCase("setvoidkillheight")) {
                    if(!plugin.getStorageConfig().isSet("maps." + args[2])) {
                        player.sendMessage(plugin.getTranslation("setup.noSuchMap"));
                        return false;
                    }
                    if(!isInteger(args[3])) {
                        player.sendMessage(plugin.getTranslation("setup.voidKillHeightError"));
                        return false;
                    }
                    plugin.getStorageConfig().set("maps." + args[2] + ".voidKillY", Integer.valueOf(args[3]));
                    plugin.saveStorage();
                    player.sendMessage(plugin.getTranslation("setup.voidKillHeightSet").replace("%y%", args[3]));
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 1) {
            return Arrays.asList("give", "reload", "activatebonus", "resetbonuses", "setup", "skipmap", "setmap");
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("setup")) {
                return Arrays.asList("create", "setdisplayname", "setvoidkillheight", "setspawn", "delete", "savedefaultkit");
            }
            if(args[0].equalsIgnoreCase("setmap")) {
                return getMapNameSuggestions();
            }
        }
        if(args.length == 3) {
            if(args[0].equalsIgnoreCase("setup")) {
                switch (args[1].toLowerCase()) {
                    case "delete", "setdisplayname", "setspawn", "setvoidkillheight": return getMapNameSuggestions();
                }
            }
        }
        return null;
    }

    protected ArrayList<String> getMapNameSuggestions() {
        ArrayList<String> suggestions = new ArrayList<>();
        for (FFAMap map : plugin.getLoadedMaps())
            suggestions.add(map.getWorld().getName());
        return suggestions;
    }

    protected void giveItem(CommandSender player, String[] args) {
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if(target == null) {
            player.sendMessage(plugin.getTranslation("noSuchPlayer"));
            return;
        }
        if(!plugin.getConfig().isSet("items." + args[2])) {
            player.sendMessage(plugin.getTranslation("noSuchItem"));
            return;
        }
        if(!isInteger(args[3])) {
            player.sendMessage(plugin.getTranslation("setup.voidKillHeightError"));
            return;
        }
        ItemStack is = plugin.getSpecialItem(args[2]);
        is.setAmount(Integer.parseInt(args[3]));
        target.getInventory().addItem(is);
        player.sendMessage(plugin.getTranslation("getItem").replace("%item%", args[2]).replace("%amount%", args[3]).replace("%player%", target.getName()));
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
