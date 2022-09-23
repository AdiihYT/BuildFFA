package eu.adiih.buildffa.listeners;

import eu.adiih.buildffa.FFAPlugin;
import eu.adiih.buildffa.utils.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameplayListeners implements Listener {

    private final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if(!event.getBlock().getType().equals(Material.GREEN_CONCRETE)) return;
        Block block = event.getBlock();
        PlayerInventory inv = event.getPlayer().getInventory();
        if(inv.getItemInMainHand().getType().equals(Material.GREEN_CONCRETE)) {
            inv.getItemInMainHand().setAmount(64);
        }
        if(inv.getItemInOffHand().getType().equals(Material.GREEN_CONCRETE)) {
            inv.getItemInOffHand().setAmount(64);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (block.getType()) {
                    case GREEN_CONCRETE -> block.setType(Material.YELLOW_CONCRETE);
                    case YELLOW_CONCRETE -> block.setType(Material.ORANGE_CONCRETE);
                    case ORANGE_CONCRETE -> block.setType(Material.RED_CONCRETE);
                    case RED_CONCRETE -> {
                        block.setType(Material.AIR);
                        this.cancel();
                    }
                    default -> this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player attacker = victim.getKiller();
        plugin.getKillStreaks().put(victim.getUniqueId(), 0);
        if(attacker == null) return;
        attacker.setHealth(20D);
        if(plugin.getKillStreaks().containsKey(attacker.getUniqueId())) {
            plugin.getKillStreaks().put(attacker.getUniqueId(), plugin.getKillStreaks().get(attacker.getUniqueId()) + 1);
        } else {
            plugin.getKillStreaks().put(attacker.getUniqueId(), 1);
        }
        for(String command : plugin.getConfig().getStringList("killCommands")) {
            if(command.startsWith("msgattacker:")) {
                attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', command.split("msgattacker:")[1]));
            }
            if(command.startsWith("msgvictim:")) {
                victim.sendMessage(ChatColor.translateAlternateColorCodes('&', command.split("msgvictim:")[1]));
            }
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%attacker%", attacker.getName()).replace("%victim%", victim.getName()));
        }
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(plugin.getGameNode() == null) return;
        if(event.getTo().getY() <= plugin.getGameNode().getCurrentMap().getVoidKillY()) {
            player.setHealth(0D);
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if(plugin.getGameNode() == null) return;
        Player player = event.getPlayer();
        player.teleport(plugin.getGameNode().getCurrentMap().getSpawnPoint());
        player.getInventory().clear();
        player.getInventory().setContents(InventoryUtils.loadInventoryContents());
        if(plugin.getTimeBar() != null) {
            plugin.getTimeBar().addPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if(plugin.getTimeBar() != null) {
            plugin.getTimeBar().getBar().removePlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent event) {
        if(plugin.getGameNode() == null) return;
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.getInventory().setContents(InventoryUtils.loadInventoryContents());
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(plugin.getGameNode().getCurrentMap().getSpawnPoint());
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if(item == null) return;
        Player player = event.getPlayer();

        ItemStack is = plugin.getSpecialItem("trampoline");
        Material material = is.getType();
        if(!item.isSimilar(is)) return;

        event.setCancelled(true);

        if(player.hasCooldown(material)) return;
        Location loc = player.getLocation().subtract(0, 2, 0);

        player.setVelocity(new Vector(0, 2, 0));
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);

        player.setCooldown(material, 5 * 20);
        item.setAmount(event.getItem().getAmount() - 1);

        for(Block block : getBlocksInRadius(loc.getBlock(), 3)) {
            if(!block.getType().equals(Material.AIR)) return;
        }

        Set<Block> center = new HashSet<>();
        center.add(loc.clone().add(-1, 0, 0).getBlock());
        center.add(loc.clone().add(0, 0, -1).getBlock());
        center.add(loc.clone().add(1, 0, 0).getBlock());
        center.add(loc.clone().add(0, 0, 1).getBlock());

        Set<Block> inner = new HashSet<>();
        // Ends
        inner.add(loc.clone().add(2, 0, 0).getBlock());
        inner.add(loc.clone().add(0, 0, 2).getBlock());
        inner.add(loc.clone().add(-2, 0, 0).getBlock());
        inner.add(loc.clone().add(0, 0, -2).getBlock());
        // Corners
        inner.add(loc.clone().add(-1, 0, -1).getBlock());
        inner.add(loc.clone().add(1, 0, 1).getBlock());
        inner.add(loc.clone().add(-1, 0, 1).getBlock());
        inner.add(loc.clone().add(1, 0, -1).getBlock());

        loc.getBlock().setType(Material.SLIME_BLOCK);
        center.forEach(block -> block.setType(Material.RED_CONCRETE));
        inner.forEach(block -> block.setType(Material.YELLOW_CONCRETE));

        new BukkitRunnable() {
            @Override
            public void run() {
                loc.getBlock().setType(Material.AIR);
                center.forEach(block -> block.setType(Material.AIR));
                inner.forEach(block -> block.setType(Material.AIR));
            }
        }.runTaskLater(plugin, 30L);
    }

    public ArrayList<Block> getBlocksInRadius(Block start, int radius){
        ArrayList<Block> blocks = new ArrayList<>();
        double startX = start.getLocation().getX();
        double startY = start.getLocation().getY();
        double startZ = start.getLocation().getZ();
        for(double x = startX - radius; x <= startX + radius; x++){
            for(double y = startY - radius; y <= startY + radius; y++){
                for(double z = startZ - radius; z <= startZ + radius; z++){
                    Location loc = new Location(start.getWorld(), x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }
        return blocks;
    }

}
