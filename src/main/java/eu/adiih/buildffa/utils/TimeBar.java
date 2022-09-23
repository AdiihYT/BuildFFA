package eu.adiih.buildffa.utils;

import eu.adiih.buildffa.FFAPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;

public class TimeBar {

    private final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);
    private BossBar bar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    public void addPlayer(Player player) {
        bar.addPlayer(player);
    }

    public BossBar getBar() {
        return bar;
    }

    public void createBar() {
        bar = Bukkit.createBossBar(
            "§7§oBetöltés...",
            BarColor.valueOf(plugin.getConfig().getString("bossbar.color")),
            BarStyle.valueOf(plugin.getConfig().getString("bossbar.style"))
        );
        bar.setVisible(true);
    }

    public void updateTitle() {
        bar.setTitle(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bossbar.text")).replace("%map%", plugin.getGameNode().getCurrentMap().getDisplayName()).replace("%expiry%", dateFormat.format(plugin.getMapExpirySeconds() * 1000)));
    }

    public void destroyBar() {
        bar.removeAll();
        bar.setVisible(false);
    }

}
