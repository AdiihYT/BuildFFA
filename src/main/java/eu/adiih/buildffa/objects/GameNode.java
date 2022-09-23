package eu.adiih.buildffa.objects;

import eu.adiih.buildffa.FFAPlugin;
import eu.adiih.buildffa.enums.Bonus;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public class GameNode {

    private final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);

    private FFAMap currentMap;
    private Set<Bonus> activeBonuses;

    public GameNode(FFAMap currentMap, Set<Bonus> activeBonuses) {
        this.currentMap = currentMap;
        this.activeBonuses = activeBonuses;
    }

    public void applyWorldSettings(World world) {
        world.getWorldBorder().setCenter(currentMap.getSpawnPoint());
        world.getWorldBorder().setSize(300D);
        world.setTime(6000L);
        world.setStorm(false);
        String rule;
        String value;
        for(String line : plugin.getConfig().getStringList("general.defaultGameRules")) {
            rule = line.split(":")[0];
            value = line.split(":")[1];
            world.setGameRuleValue(rule, value);
            plugin.getLogger().info("Setting " + rule + " to " + value + " in world " + world.getName());
        }
    }

    public void changeMap(FFAMap map) {
        activeBonuses.clear();
        setCurrentMap(map);
        applyWorldSettings(map.getWorld());
        for(Player player : plugin.getServer().getOnlinePlayers()) player.teleport(map.getSpawnPoint());
        plugin.setMapExpirySeconds(plugin.getConfig().getInt("general.mapCycleDuration"));
    }

    public FFAMap getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(FFAMap currentMap) {
        this.currentMap = currentMap;
    }

    public Set<Bonus> getActiveBonuses() {
        return activeBonuses;
    }

    public void setActiveBonuses(Set<Bonus> activeBonuses) {
        this.activeBonuses = activeBonuses;
    }

    @Override
    public String toString() {
        return "GameNode{" +
                "currentMap=" + currentMap.toString() +
                ", activeBonuses=" + activeBonuses +
                '}';
    }
}
