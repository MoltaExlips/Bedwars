package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Game;
import com.bedwars.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    private final BedwarsPlugin plugin;
    
    public PlayerListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Welcome message
        player.sendMessage("§8[§cBedwars§8] §aWelcome to Bedwars!");
        player.sendMessage("§7Use §e/bedwars §7to join a game!");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null) {
            // Remove player from game
            plugin.getGameManager().leaveGame(player);
        }
    }
} 