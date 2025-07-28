package com.bedwars.game;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit {
    
    private final String id;
    private final String name;
    private final String description;
    private final List<ItemStack> items;
    
    public Kit(String id, String name, String description, List<ItemStack> items) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.items = items;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<ItemStack> getItems() {
        return items;
    }
    
    public void giveItems(org.bukkit.entity.Player player) {
        for (ItemStack item : items) {
            player.getInventory().addItem(item);
        }
    }
} 