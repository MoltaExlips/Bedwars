package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Game;
import com.bedwars.game.GameState;
import com.bedwars.game.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class BedListener implements Listener {
    
    private final BedwarsPlugin plugin;
    
    public BedListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null && game.getState() == GameState.PLAYING) {
            // Cancel bed sleeping in Bedwars
            event.setUseBed(PlayerBedEnterEvent.Result.DENY);
            player.sendMessage("§cYou cannot sleep in Bedwars!");
        }
    }
    
    @EventHandler
    public void onBedBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        
        if (game != null && game.getState() == GameState.PLAYING) {
            if (isBed(block.getType())) {
                // Handle bed breaking in GameListener
                // This is just a placeholder to prevent direct bed breaking
                event.setCancelled(true);
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
} 