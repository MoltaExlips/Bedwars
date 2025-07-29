package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {
    
    private final BedwarsPlugin plugin;
    
    public LobbyListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.contains("Bedwars")) {
            event.setCancelled(true);
            
            if (title.contains("Main Menu")) {
                handleMainMenuClick(player, event.getCurrentItem());
            } else if (title.contains("Select Map")) {
                handleMapSelectionClick(player, event.getSlot());
            } else if (title.contains("Select Team")) {
                handleTeamSelectionClick(player, event.getSlot());
            } else if (title.contains("Shop")) {
                handleShopClick(player, event.getSlot());
            }
        }
    }
    
    private void handleMainMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null) return;
        
        String itemName = clickedItem.getItemMeta().getDisplayName();
        
        if (itemName.contains("Join Game")) {
            plugin.getLobbyManager().openMapSelection(player);
        } else if (itemName.contains("Leave Game")) {
            plugin.getGameManager().leaveGame(player);
            player.sendMessage("§aYou left the game!");
        }
    }
    
    private void handleMapSelectionClick(Player player, int slot) {
        // Get available maps
        java.util.Collection<com.bedwars.game.Map> maps = plugin.getMapManager().getMaps();
        String[] mapIds = maps.stream().map(com.bedwars.game.Map::getId).toArray(String[]::new);
        
        if (slot < mapIds.length) {
            String selectedMap = mapIds[slot];
            plugin.getLobbyManager().setPlayerSelection(player.getUniqueId(), "map", selectedMap);
            plugin.getLobbyManager().openTeamSelection(player);
        }
    }
    
    private void handleTeamSelectionClick(Player player, int slot) {
        String[] teams = {"red", "blue", "green", "yellow"};
        
        if (slot < teams.length) {
            String selectedTeam = teams[slot];
            plugin.getLobbyManager().setPlayerSelection(player.getUniqueId(), "team", selectedTeam);
            
            // Try to join game with complete selection
            if (plugin.getLobbyManager().joinGameWithSelection(player)) {
                player.sendMessage("§aSuccessfully joined the game!");
            } else {
                player.sendMessage("§cFailed to join game. Please try again.");
            }
        }
    }
    
    private void handleShopClick(Player player, int slot) {
        String[] categories = {"blocks", "weapons", "armor", "tools", "utilities"};
        
        if (slot == 10) {
            plugin.getShopManager().openShopMainMenu(player);
        } else if (slot == 12) {
            plugin.getShopManager().openShopMainMenu(player);
        } else if (slot == 14) {
            plugin.getShopManager().openShopMainMenu(player);
        } else if (slot == 16) {
            plugin.getShopManager().openShopMainMenu(player);
        } else if (slot == 28) {
            plugin.getShopManager().openShopMainMenu(player);
        }
    }
} 