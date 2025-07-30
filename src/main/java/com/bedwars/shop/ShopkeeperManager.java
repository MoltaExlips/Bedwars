package com.bedwars.shop;

import com.bedwars.BedwarsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopkeeperManager {
    
    private final BedwarsPlugin plugin;
    private final Map<String, UUID> teamShopkeepers = new HashMap<>();
    
    public ShopkeeperManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void spawnShopkeeper(String teamColor, Location location) {
        // Remove existing shopkeeper for this team
        removeShopkeeper(teamColor);
        
        // Spawn new shopkeeper
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setCustomName("§" + getTeamColorCode(teamColor) + teamColor.toUpperCase() + " Shopkeeper");
        villager.setCustomNameVisible(true);
        villager.setProfession(org.bukkit.entity.Villager.Profession.LIBRARIAN);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setMetadata("bedwars_team", new FixedMetadataValue(plugin, teamColor));
        
        // Register with shop manager
        plugin.getShopManager().registerShopkeeper(villager);
        
        // Store reference
        teamShopkeepers.put(teamColor, villager.getUniqueId());
    }
    
    public void removeShopkeeper(String teamColor) {
        UUID shopkeeperId = teamShopkeepers.get(teamColor);
        if (shopkeeperId != null) {
            org.bukkit.entity.Entity entity = plugin.getServer().getEntity(shopkeeperId);
            if (entity != null && entity instanceof Villager) {
                entity.remove();
            }
            teamShopkeepers.remove(teamColor);
        }
    }
    
    public void removeAllShopkeepers() {
        for (String teamColor : teamShopkeepers.keySet()) {
            removeShopkeeper(teamColor);
        }
    }
    
    public Villager getShopkeeper(String teamColor) {
        UUID shopkeeperId = teamShopkeepers.get(teamColor);
        if (shopkeeperId != null) {
            org.bukkit.entity.Entity entity = plugin.getServer().getEntity(shopkeeperId);
            if (entity instanceof Villager) {
                return (Villager) entity;
            }
        }
        return null;
    }
    
    private String getTeamColorCode(String color) {
        switch (color.toLowerCase()) {
            case "red": return "c";
            case "blue": return "9";
            case "green": return "a";
            case "yellow": return "e";
            default: return "f";
        }
    }
} 