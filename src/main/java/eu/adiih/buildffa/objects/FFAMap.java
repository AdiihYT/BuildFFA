package eu.adiih.buildffa.objects;

import org.bukkit.Location;
import org.bukkit.World;

public class FFAMap {

    private World world;
    private String displayName;
    private int voidKillY;
    private Location spawnPoint;

    public FFAMap(World world, String displayName, int voidKillY, Location spawnPoint) {
        this.world = world;
        this.displayName = displayName;
        this.voidKillY = voidKillY;
        this.spawnPoint = spawnPoint;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getVoidKillY() {
        return voidKillY;
    }

    public void setVoidKillY(int voidKillY) {
        this.voidKillY = voidKillY;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public String toString() {
        return "FFAMap{" +
                "world=" + world +
                ", displayName='" + displayName + '\'' +
                ", voidKillY=" + voidKillY +
                ", spawnPoint=" + spawnPoint +
                '}';
    }
}
