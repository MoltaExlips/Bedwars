package com.bedwars.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;

public class Map {
    
    private final String id;
    private final String name;
    private final String description;
    private final int minPlayers;
    private final int maxPlayers;
    private final int teams;
    private final java.util.Map<String, Location> spawnPoints;
    private final java.util.Map<String, Location> bedLocations;
    private final java.util.Map<String, List<Location>> generators;
    private World world;
    private boolean active;
    
    public Map(String id, String name, String description, int minPlayers, int maxPlayers, int teams,
               java.util.Map<String, Location> spawnPoints, java.util.Map<String, Location> bedLocations, java.util.Map<String, List<Location>> generators) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.teams = teams;
        this.spawnPoints = new HashMap<>(spawnPoints);
        this.bedLocations = new HashMap<>(bedLocations);
        this.generators = new HashMap<>(generators);
        this.active = false;
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
    
    public int getMinPlayers() {
        return minPlayers;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public int getTeams() {
        return teams;
    }
    
    public java.util.Map<String, Location> getSpawnPoints() {
        return new HashMap<>(spawnPoints);
    }
    
    public Location getSpawnPoint(String team) {
        return spawnPoints.get(team);
    }
    
    public java.util.Map<String, Location> getBedLocations() {
        return new HashMap<>(bedLocations);
    }
    
    public Location getBedLocation(String team) {
        return bedLocations.get(team);
    }
    
    public java.util.Map<String, List<Location>> getGenerators() {
        return new HashMap<>(generators);
    }
    
    public List<Location> getGeneratorLocations(String type) {
        return generators.get(type);
    }
    
    public World getWorld() {
        return world;
    }
    
    public void setWorld(World world) {
        this.world = world;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setupWorld() {
        if (world != null) {
            // Clear the world and set it up for Bedwars
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);
            
            // Set up beds
            for (java.util.Map.Entry<String, Location> entry : bedLocations.entrySet()) {
                Location loc = entry.getValue();
                if (loc != null && loc.getWorld() != null) {
                    loc.getBlock().setType(Material.RED_BED);
                    loc.clone().add(0, 0, 1).getBlock().setType(Material.RED_BED);
                }
            }
            
            // Set up generators
            for (java.util.Map.Entry<String, List<Location>> entry : generators.entrySet()) {
                String type = entry.getKey();
                List<Location> locations = entry.getValue();
                
                for (Location loc : locations) {
                    if (loc != null && loc.getWorld() != null) {
                        switch (type.toLowerCase()) {
                            case "iron":
                                loc.getBlock().setType(Material.IRON_BLOCK);
                                break;
                            case "gold":
                                loc.getBlock().setType(Material.GOLD_BLOCK);
                                break;
                            case "diamond":
                                loc.getBlock().setType(Material.DIAMOND_BLOCK);
                                break;
                            case "emerald":
                                loc.getBlock().setType(Material.EMERALD_BLOCK);
                                break;
                        }
                    }
                }
            }
        }
    }
    
    public void resetWorld() {
        if (world != null) {
            // Reset the world to its original state
            // This would typically involve restoring from a backup or template
            // For now, we'll just clear the world
            world.getEntities().forEach(entity -> {
                if (!(entity instanceof org.bukkit.entity.Player)) {
                    entity.remove();
                }
            });
            
            // Clear all blocks in the game area
            // This is a simplified approach - in a real implementation,
            // you'd want to restore from a template world
            for (int x = -200; x <= 200; x++) {
                for (int y = 0; y <= 128; y++) {
                    for (int z = -200; z <= 200; z++) {
                        Location loc = new Location(world, x, y, z);
                        if (loc.getBlock().getType() != Material.AIR) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
} 