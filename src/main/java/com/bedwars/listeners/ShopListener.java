package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {
    
    private final BedwarsPlugin plugin;
    
    public ShopListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.contains("Shop") && !title.contains("Select")) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName != null && !itemName.contains("Click to")) {
                    // Extract item ID from display name
                    String itemId = itemName.replace("§6", "").replace(" ", "_").toLowerCase();
                }
            }
        }
    }
} 