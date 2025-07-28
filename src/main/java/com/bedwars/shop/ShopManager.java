package com.bedwars.shop;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Game;
import com.bedwars.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    
    public ShopManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    public void openShopCategory(Player player, String category) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8[§cBedwars§8] " + category.substring(0, 1).toUpperCase() + category.substring(1));
        
        Map<String, ConfigManager.ShopItem> shopItems = configManager.getShopItems();
        int slot = 0;
        
        for (Map.Entry<String, ConfigManager.ShopItem> entry : shopItems.entrySet()) {
            String itemId = entry.getKey();
            ConfigManager.ShopItem item = entry.getValue();
            
            // Check if item belongs to this category
            if (itemId.startsWith(category + "_")) {
                ItemStack displayItem = new ItemStack(item.getMaterial(), item.getAmount());
                ItemMeta meta = displayItem.getItemMeta();
                meta.setDisplayName("§6" + itemId.replace("_", " ").toUpperCase());
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Cost: §e" + item.getCost() + " " + item.getCurrency().name().replace("_", " "));
                lore.add("§7Click to purchase");
                meta.setLore(lore);
                
                displayItem.setItemMeta(meta);
                inv.setItem(slot, displayItem);
                slot++;
            }
        }
        
        player.openInventory(inv);
    }
    
    public boolean purchaseItem(Player player, String itemId) {
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game == null || game.getState() != com.bedwars.game.GameState.PLAYING) {
            return false;
        }
        
        Map<String, ConfigManager.ShopItem> shopItems = configManager.getShopItems();
        ConfigManager.ShopItem item = shopItems.get(itemId);
        
        if (item == null) {
            return false;
        }
        
        // Check if player has enough currency
        if (!hasEnoughCurrency(player, item.getCurrency(), item.getCost())) {
            player.sendMessage(configManager.getMessage("shop.insufficient-funds"));
            return false;
        }
        
        // Remove currency from player
        removeCurrency(player, item.getCurrency(), item.getCost());
        
        // Give item to player
        ItemStack purchasedItem = new ItemStack(item.getMaterial(), item.getAmount());
        player.getInventory().addItem(purchasedItem);
        
        // Send confirmation message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemId.replace("_", " "));
        placeholders.put("cost", item.getCost() + " " + item.getCurrency().name().replace("_", " "));
        player.sendMessage(configManager.getMessage("shop.purchased", placeholders));
        
        return true;
    }
    
    private boolean hasEnoughCurrency(Player player, Material currency, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }
    
    private void removeCurrency(Player player, Material currency, int amount) {
        int remaining = amount;
        
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == currency) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
    }
    
    public int getPlayerCurrency(Player player, Material currency) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    public void displayPlayerCurrency(Player player) {
        int iron = getPlayerCurrency(player, Material.IRON_INGOT);
        int gold = getPlayerCurrency(player, Material.GOLD_INGOT);
        int diamond = getPlayerCurrency(player, Material.DIAMOND);
        int emerald = getPlayerCurrency(player, Material.EMERALD);
        
        player.sendMessage("§8[§cBedwars§8] §7Your currency:");
        player.sendMessage("§7Iron: §f" + iron);
        player.sendMessage("§7Gold: §6" + gold);
        player.sendMessage("§7Diamond: §b" + diamond);
        player.sendMessage("§7Emerald: §a" + emerald);
    }
} 