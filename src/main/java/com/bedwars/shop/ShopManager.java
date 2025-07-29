package com.bedwars.shop;

import com.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopManager implements Listener {
    
    private final BedwarsPlugin plugin;
    // You can use a UUID or custom tag to identify your shopkeeper(s)
    private final Set<UUID> shopkeeperUUIDs = new HashSet<>();
    
    public ShopManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    // Call this when you spawn your shopkeeper(s)
    public void registerShopkeeper(Villager villager) {
        shopkeeperUUIDs.add(villager.getUniqueId());
    }

    // 1. Open main shop menu on right-click
    @EventHandler
    public void onShopkeeperRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager &&
            shopkeeperUUIDs.contains(event.getRightClicked().getUniqueId())) {
            event.setCancelled(true);
            openShopMainMenu((Player) event.getPlayer());
        }
    }

    // 2. Main shop menu with categories
    public void openShopMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Shop");

        inv.setItem(10, createMenuItem(Material.BRICKS, "§aBlocks", "§7Buy building blocks"));
        inv.setItem(12, createMenuItem(Material.IRON_SWORD, "§bSwords & Armor", "§7Buy weapons and armor"));
        inv.setItem(14, createMenuItem(Material.WOODEN_PICKAXE, "§6Tools", "§7Buy tools"));
        inv.setItem(16, createMenuItem(Material.TNT, "§dMisc", "§7Buy miscellaneous items"));

        player.openInventory(inv);
    }

    // 3. Category GUIs
    private void openBlocksMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Blocks");

        inv.setItem(10, createShopItem(Material.WHITE_WOOL, 16, "§fWool x16", "§78 Iron", Material.IRON_INGOT, 8));
        inv.setItem(12, createShopItem(Material.SANDSTONE, 12, "§eSandstone x12", "§78 Iron", Material.IRON_INGOT, 8));
        inv.setItem(14, createShopItem(Material.END_STONE, 24, "§6Endstone x24", "§78 Iron", Material.IRON_INGOT, 8));
        inv.setItem(26, createMenuItem(Material.BARRIER, "§cBack", "§7Return to main shop"));

        player.openInventory(inv);
    }

    private void openToolsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Tools");

        inv.setItem(10, createEnchantedShopItem(Material.WOODEN_PICKAXE, 1, "§fWooden Pickaxe", "§730 Iron", Material.IRON_INGOT, 30));
        inv.setItem(12, createEnchantedShopItem(Material.WOODEN_AXE, 1, "§fWooden Axe", "§730 Iron", Material.IRON_INGOT, 30));
        inv.setItem(26, createMenuItem(Material.BARRIER, "§cBack", "§7Return to main shop"));

        player.openInventory(inv);
    }

    private void openSwordsArmorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Swords & Armor");
        // Add your swords and armor here
        inv.setItem(26, createMenuItem(Material.BARRIER, "§cBack", "§7Return to main shop"));
        player.openInventory(inv);
    }

    private void openMiscMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Misc");
        // Add your misc items here
        inv.setItem(26, createMenuItem(Material.BARRIER, "§cBack", "§7Return to main shop"));
        player.openInventory(inv);
    }
    
    // 4. Handle GUI clicks and purchases
    @EventHandler
    public void onShopGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Only handle our shop GUIs
        if (!title.contains("Bedwars")) return;

        event.setCancelled(true); // Prevent taking items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Main menu navigation
        if (title.contains("Shop")) {
            switch (clicked.getType()) {
                case BRICKS: openBlocksMenu(player); break;
                case IRON_SWORD: openSwordsArmorMenu(player); break;
                case WOODEN_PICKAXE: openToolsMenu(player); break;
                case TNT: openMiscMenu(player); break;
            }
            return;
        }

        // Back button
        if (clicked.getType() == Material.BARRIER) {
            openShopMainMenu(player);
            return;
        }

        // Blocks shop
        if (title.contains("Blocks")) {
            if (clicked.getType() == Material.WHITE_WOOL) {
                buyItem(player, Material.IRON_INGOT, 8, new ItemStack(Material.WHITE_WOOL, 16), "Wool x16");
            } else if (clicked.getType() == Material.SANDSTONE) {
                buyItem(player, Material.IRON_INGOT, 8, new ItemStack(Material.SANDSTONE, 12), "Sandstone x12");
            } else if (clicked.getType() == Material.END_STONE) {
                buyItem(player, Material.IRON_INGOT, 8, new ItemStack(Material.END_STONE, 24), "Endstone x24");
            }
            return;
        }

        // Tools shop
        if (title.contains("Tools")) {
            if (clicked.getType() == Material.WOODEN_PICKAXE) {
                ItemStack pick = new ItemStack(Material.WOODEN_PICKAXE, 1);
                pick.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1);
                buyItem(player, Material.IRON_INGOT, 30, pick, "Wooden Pickaxe (Efficiency I)");
            } else if (clicked.getType() == Material.WOODEN_AXE) {
                ItemStack axe = new ItemStack(Material.WOODEN_AXE, 1);
                axe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1);
                buyItem(player, Material.IRON_INGOT, 30, axe, "Wooden Axe (Efficiency I)");
            }
            return;
        }

        // Swords & Armor and Misc can be filled similarly
    }

    // 5. Purchase logic
    private void buyItem(Player player, Material currency, int cost, ItemStack item, String name) {
        int playerCurrency = getPlayerCurrency(player, currency);
        if (playerCurrency < cost) {
            player.sendMessage("§cYou don't have enough " + currency.name().replace("_", " ").toLowerCase() + "!");
            return;
        }
        removeCurrency(player, currency, cost);
        player.getInventory().addItem(item);
        player.sendMessage("§aPurchased: " + name + " for " + cost + " " + currency.name().replace("_", " ").toLowerCase());
    }

    // 6. Utility methods
    private ItemStack createMenuItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createShopItem(Material mat, int amount, String name, String price, Material currency, int cost) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(price, "§7Click to buy"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEnchantedShopItem(Material mat, int amount, String name, String price, Material currency, int cost) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(price, "§7Click to buy"));
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1);
        return item;
    }

    private int getPlayerCurrency(Player player, Material currency) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currency) {
                count += item.getAmount();
            }
        }
        return count;
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
} 