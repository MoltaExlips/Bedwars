package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Game;
import com.bedwars.game.GameState;
import com.bedwars.game.Team;
import com.bedwars.utils.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.Map;

public class GameListener implements Listener {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    
    public GameListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null && game.getState() == GameState.PLAYING) {
            // Handle player death in game
            game.killPlayer(player);
            
            // Don't drop items in Bedwars
            event.getDrops().clear();
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null) {
            plugin.getGameManager().leaveGame(player);
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null && game.getState() == GameState.PLAYING) {
            // Check if player is breaking a bed
            if (isBed(block.getType())) {
                handleBedBreak(player, block, game);
                return;
            }
            
            // Check if player is breaking blocks in their own base
            Team playerTeam = game.getPlayerTeam(player.getUniqueId());
            if (playerTeam != null && isInTeamBase(block.getLocation(), playerTeam, game)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot break blocks in your own base!");
                return;
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null && game.getState() == GameState.PLAYING) {
            // Allow block placement in game
            // Could add restrictions here if needed
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null && game.getState() == GameState.PLAYING) {
            // Handle shop interactions
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                Block block = event.getClickedBlock();
                if (block != null && block.getType() == Material.CHEST) {
                    // Open shop
                    plugin.getShopManager().openShopMainMenu(player);
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            
            Game game = plugin.getGameManager().getPlayerGame(attacker.getUniqueId());
            if (game != null && game.getState() == GameState.PLAYING) {
                // Check if players are on the same team
                Team attackerTeam = game.getPlayerTeam(attacker.getUniqueId());
                Team victimTeam = game.getPlayerTeam(victim.getUniqueId());
                
                if (attackerTeam != null && victimTeam != null && attackerTeam.equals(victimTeam)) {
                    event.setCancelled(true);
                    attacker.sendMessage("§cYou cannot attack your teammates!");
                }
            }
        }
    }
    
    private boolean isBed(Material material) {
        return material == Material.RED_BED || material == Material.BLUE_BED ||
               material == Material.GREEN_BED || material == Material.YELLOW_BED ||
               material == Material.WHITE_BED || material == Material.ORANGE_BED ||
               material == Material.MAGENTA_BED || material == Material.LIGHT_BLUE_BED ||
               material == Material.YELLOW_BED || material == Material.LIME_BED ||
               material == Material.PINK_BED || material == Material.GRAY_BED ||
               material == Material.LIGHT_GRAY_BED || material == Material.CYAN_BED ||
               material == Material.PURPLE_BED || material == Material.BLUE_BED ||
               material == Material.BROWN_BED || material == Material.GREEN_BED ||
               material == Material.RED_BED || material == Material.BLACK_BED;
    }
    
    private void handleBedBreak(Player player, Block bedBlock, Game game) {
        // Find which team's bed this is
        for (Map.Entry<String, Team> entry : game.getTeams().entrySet()) {
            String teamName = entry.getKey();
            Team team = entry.getValue();
            
            Location bedLocation = team.getBedLocation();
            if (bedLocation != null && isNearLocation(bedBlock.getLocation(), bedLocation, 2)) {
                // Check if player is on the same team
                Team playerTeam = game.getPlayerTeam(player.getUniqueId());
                if (playerTeam != null && playerTeam.equals(team)) {
                    player.sendMessage("§cYou cannot break your own bed!");
                    return;
                }
                
                // Destroy the bed
                game.destroyBed(teamName);
                
                // Update player stats
                UUID playerId = player.getUniqueId();
                ConfigManager.PlayerStats stats = configManager.getPlayerStats(playerId);
                stats.setBedsDestroyed(stats.getBedsDestroyed() + 1);
                configManager.savePlayerStats(playerId, stats);
                
                return;
            }
        }
    }
    
    private boolean isInTeamBase(Location location, Team team, Game game) {
        Location spawnLocation = team.getSpawnLocation();
        if (spawnLocation != null) {
            return isNearLocation(location, spawnLocation, 10);
        }
        return false;
    }
    
    private boolean isNearLocation(Location loc1, Location loc2, double distance) {
        if (loc1.getWorld() != loc2.getWorld()) {
            return false;
        }
        
        double dx = loc1.getX() - loc2.getX();
        double dy = loc1.getY() - loc2.getY();
        double dz = loc1.getZ() - loc2.getZ();
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= distance;
    }
} 