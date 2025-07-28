package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Game;
import com.bedwars.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class GeneratorListener implements Listener {
    
    private final BedwarsPlugin plugin;
    
    public GeneratorListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game != null && game.getState() == GameState.PLAYING) {
            // Allow pickup of generator items
            Material itemMaterial = item.getItemStack().getType();
            if (isGeneratorItem(itemMaterial)) {
                // Item is from a generator, allow pickup
                return;
            }
        }
    }
    
    private boolean isGeneratorItem(Material material) {
        return material == Material.IRON_INGOT || 
               material == Material.GOLD_INGOT || 
               material == Material.DIAMOND || 
               material == Material.EMERALD;
    }
} 