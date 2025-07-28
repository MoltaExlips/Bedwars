package com.bedwars.game;

import com.bedwars.BedwarsPlugin;
import com.bedwars.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.HashMap;

public class MapManager {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    private final java.util.Map<String, Map> maps;
    
    public MapManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.maps = new HashMap<>();
    }
    
    public void loadMaps() {
        java.util.Map<String, Map> configMaps = configManager.getMaps();
        
        for (java.util.Map.Entry<String, Map> entry : configMaps.entrySet()) {
            String mapId = entry.getKey();
            Map map = entry.getValue();
            
            // Create world for the map
            World world = createWorldForMap(mapId);
            if (world != null) {
                map.setWorld(world);
                maps.put(mapId, map);
                plugin.getLogger().info("Loaded map: " + mapId);
            }
        }
    }
    
    private World createWorldForMap(String mapId) {
        String worldName = "bedwars_" + mapId;
        
        // Check if world already exists
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            return existingWorld;
        }
        
        // Create new world
        WorldCreator creator = new WorldCreator(worldName);
        creator.type(org.bukkit.WorldType.FLAT);
        creator.generateStructures(false);
        creator.generatorSettings("minecraft:bedrock,2*minecraft:stone,minecraft:grass_block");
        
        World world = creator.createWorld();
        if (world != null) {
            // Set world properties
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doWeatherCycle", "false");
            world.setGameRuleValue("keepInventory", "false");
            world.setGameRuleValue("naturalRegeneration", "false");
            
            return world;
        }
        
        return null;
    }
    
    public Map getMap(String mapId) {
        return maps.get(mapId);
    }
    
    public java.util.Map<String, Map> getMaps() {
        return new HashMap<>(maps);
    }
    
    public Map getAvailableMap(String mapId) {
        Map map = maps.get(mapId);
        if (map != null && !map.isActive()) {
            return map;
        }
        return null;
    }
    
    public void setMapActive(String mapId, boolean active) {
        Map map = maps.get(mapId);
        if (map != null) {
            map.setActive(active);
        }
    }
    
    public void resetMap(String mapId) {
        Map map = maps.get(mapId);
        if (map != null) {
            map.resetWorld();
            map.setActive(false);
        }
    }
    
    public void unloadMaps() {
        for (Map map : maps.values()) {
            if (map.getWorld() != null) {
                Bukkit.unloadWorld(map.getWorld(), false);
            }
        }
        maps.clear();
    }
} 