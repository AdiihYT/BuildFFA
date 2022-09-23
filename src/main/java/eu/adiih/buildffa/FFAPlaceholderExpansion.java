package eu.adiih.buildffa;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class FFAPlaceholderExpansion extends PlaceholderExpansion {

    private final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) return "NULL";
        switch(params.toLowerCase()) {
            case "currentmap":
            case "currentmap_world":
                return plugin.getGameNode().getCurrentMap().getWorld().getName();
            case "currentmap_displayname":
                return plugin.getGameNode().getCurrentMap().getDisplayName();
            case "currentmap_expiry_formatted":
                return dateFormat.format(plugin.getMapExpirySeconds() * 1000);
            case "currentmap_expiry_seconds":
                return String.valueOf(plugin.getMapExpirySeconds());
            case "currentmap_expiry_minutes":
                return String.valueOf(plugin.getMapExpirySeconds() / 60 == 0 ? "<1" : plugin.getMapExpirySeconds() / 60);
            case "killstreak":
                return plugin.getKillStreaks().get(player.getUniqueId()) == null ? "0" : String.valueOf(plugin.getKillStreaks().get(player.getUniqueId()));
        }
        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "buildffa";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AdiihYT";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }
}
