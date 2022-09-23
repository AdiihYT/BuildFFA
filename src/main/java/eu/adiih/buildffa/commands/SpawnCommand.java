package eu.adiih.buildffa.commands;

import eu.adiih.buildffa.FFAPlugin;
import eu.adiih.buildffa.objects.FFAMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand implements CommandExecutor, TabCompleter {

    private final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return false;
        if(args.length >= 1) {
            if(!plugin.getStorageConfig().isSet("maps." + args[0])) {
                player.sendMessage(plugin.getTranslation("setup.noSuchMap"));
                return false;
            }
            player.teleport(plugin.getStorageConfig().getLocation("maps." + args[0] + ".spawnPoint"));
        }
        player.teleport(plugin.getGameNode().getCurrentMap().getSpawnPoint());
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!sender.hasPermission("buildffa.admin")) return null;
        if(args.length == 1) {
            ArrayList<String> suggestions = new ArrayList<>();
            for (FFAMap map : plugin.getLoadedMaps())
                suggestions.add(map.getWorld().getName());
            return suggestions;
        }
        return null;
    }
}
