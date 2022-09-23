package eu.adiih.buildffa;

import eu.adiih.buildffa.commands.BuildFFACommand;
import eu.adiih.buildffa.commands.SpawnCommand;
import eu.adiih.buildffa.listeners.GameplayListeners;
import eu.adiih.buildffa.objects.FFAMap;
import eu.adiih.buildffa.objects.GameNode;
import eu.adiih.buildffa.utils.TimeBar;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class FFAPlugin extends JavaPlugin {

    private Set<FFAMap> loadedMaps;
    private Map<UUID, Integer> killStreaks;
    private GameNode gameNode;
    private int mapExpirySeconds;
    private TimeBar timeBar;
    private FileConfiguration config;
    private File storageFile;
    private File configFile;
    private FileConfiguration storageConfig;

    @Override
    public void onEnable() {
        loadedMaps = new HashSet<>();
        killStreaks = new HashMap<>();

        setupConfigurationFiles();
        registerListeners();
        registerCommands();

        if(storageConfig.getConfigurationSection("maps") == null) {
            getLogger().warning("Game node couldn't be created, because there were no maps found in storage.");
            return;
        }

        loadMapsFromStorage();
        FFAMap firstMap;
        do {
            firstMap = getRandomMap();
        } while(getServer().getWorld(firstMap.getWorld().getName()) == null);
        getLogger().info("First map is selected: " + firstMap.getWorld().getName() + " (" + firstMap.getDisplayName() + ")");

        gameNode = new GameNode(firstMap, new HashSet<>());
        gameNode.applyWorldSettings(gameNode.getCurrentMap().getWorld());
        for(Player player : getServer().getOnlinePlayers()) player.teleport(firstMap.getSpawnPoint());
        getLogger().info("Game node successfully created: " + gameNode.toString());

        mapExpirySeconds = getConfig().getInt("general.mapCycleDuration");
        startMapCycleTimer();

        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FFAPlaceholderExpansion().register();
            getLogger().info("PlaceholderAPI detected, placeholders registered.");
        }

        if(getConfig().getBoolean("bossbar.enabled")) setupBossBar();
    }

    @Override
    public void onDisable() {
        if(timeBar != null) timeBar.destroyBar();
    }

    protected void registerListeners() {
        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(new GameplayListeners(), this);
    }

    protected void registerCommands() {
        getLogger().info("Registering commands...");
        getCommand("buildffa").setExecutor(new BuildFFACommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
    }

    protected void setupBossBar() {
        timeBar = new TimeBar();
        timeBar.createBar();
        getServer().getOnlinePlayers().forEach(timeBar::addPlayer);
        getLogger().info("Bossbar successfully set up.");
    }

    protected void loadMapsFromStorage() {
        if(storageConfig.getConfigurationSection("maps") == null) return;
        getLogger().info("Found " + storageConfig.getConfigurationSection("maps").getKeys(false).size() + " map(s) in the storage, loading them...");
        loadedMaps.clear();
        for(String section : storageConfig.getConfigurationSection("maps").getKeys(false)) {
            loadedMaps.add(getMapByName(section));
        }
    }

    protected void startMapCycleTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(mapExpirySeconds <= 0) {
                    FFAMap nextMap = getRandomMap();
                    if(loadedMaps.size() > 1) {
                        while (nextMap.getWorld().getName().equals(gameNode.getCurrentMap().getWorld().getName())) nextMap = getRandomMap();
                    }
                    gameNode.changeMap(nextMap);
                    getServer().broadcastMessage(getTranslation("mapChanged").replace("%map%", nextMap.getDisplayName()));
                    return;
                }
                mapExpirySeconds--;
                if(timeBar != null) {
                    timeBar.getBar().setProgress((double) mapExpirySeconds / getConfig().getInt("general.mapCycleDuration"));
                    timeBar.updateTitle();
                }
                if(mapExpirySeconds == 600 || mapExpirySeconds == 300 || mapExpirySeconds == 120 || mapExpirySeconds == 60) {
                    getServer().broadcastMessage(getTranslation("mapChangeIncoming.minutes").replace("%minutes%", String.valueOf(mapExpirySeconds / 60)));
                }
                if((mapExpirySeconds == 30 || mapExpirySeconds <= 10) && mapExpirySeconds != 0) {
                    getServer().broadcastMessage(getTranslation("mapChangeIncoming.seconds").replace("%seconds%", String.valueOf(mapExpirySeconds)));
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    public void createNewMap(World world) {
        String worldName = world.getName();
        getStorageConfig().set("maps." + worldName + ".world", worldName);
        getStorageConfig().set("maps." + worldName + ".displayName", worldName);
        getStorageConfig().set("maps." + worldName + ".voidKillY", 0);
        getStorageConfig().set("maps." + worldName + ".spawnPoint", world.getSpawnLocation());
        saveStorage();
    }

    public String getTranslation(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages." + key).replace("%prefix%", ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix"))));
    }

    public FFAMap getRandomMap() {
        int item = new Random().nextInt(loadedMaps.size());
        int i = 0;
        for(FFAMap obj : loadedMaps) {
            if (i == item) return obj;
            i++;
        }
        return null;
    }

    public FFAMap getMapByName(String name) {
        return new FFAMap(
            getServer().getWorld(getStorageConfig().getString("maps." + name + ".world")),
            ChatColor.translateAlternateColorCodes('&', getStorageConfig().getString("maps." + name + ".displayName")),
            getStorageConfig().getInt("maps." + name + ".voidKillY"),
            getStorageConfig().getLocation("maps." + name + ".spawnPoint")
        );
    }

    protected void setupConfigurationFiles() {
        getLogger().info("Loading configuration files...");
        storageFile = new File(getDataFolder(), "storage.yml");
        configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        if(!storageFile.exists()) {
            storageFile.getParentFile().mkdirs();
            saveResource("storage.yml", false);
        }
        config = new YamlConfiguration();
        storageConfig = new YamlConfiguration();
        try {
            config.load(configFile);
            storageConfig.load(storageFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ItemStack getSpecialItem(String key) {
        ItemStack is = new ItemStack(Material.matchMaterial(getConfig().getString("items." + key + ".material")));
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("items." + key + ".displayName")));
        ArrayList<String> loreLines = new ArrayList<>();
        for(String line : getConfig().getStringList("items." + key + ".lore"))
            loreLines.add(ChatColor.translateAlternateColorCodes('&', line));
        meta.setLore(loreLines);
        is.setItemMeta(meta);
        return is;
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfigurationFiles() {
        try {
            config.load(configFile);
            storageConfig.load(storageFile);
            loadMapsFromStorage();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void saveStorage() {
        try {
            storageConfig.save(storageFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public FileConfiguration getStorageConfig() {
        return storageConfig;
    }

    public GameNode getGameNode() {
        return gameNode;
    }

    public Set<FFAMap> getLoadedMaps() {
        return loadedMaps;
    }

    public Map<UUID, Integer> getKillStreaks() {
        return killStreaks;
    }

    public int getMapExpirySeconds() {
        return mapExpirySeconds;
    }

    public void setMapExpirySeconds(int mapExpirySeconds) {
        this.mapExpirySeconds = mapExpirySeconds;
    }

    public TimeBar getTimeBar() {
        return timeBar;
    }
}
