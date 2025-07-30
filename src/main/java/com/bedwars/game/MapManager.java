package com.bedwars.game;

import com.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {
    private final BedwarsPlugin plugin;
    private final java.util.Map<String, com.bedwars.game.Map> maps = new java.util.HashMap<>();
    private final File mapsFile;
    private YamlConfiguration mapsConfig;

    public MapManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.mapsFile = new File(plugin.getDataFolder(), "maps.yml");
        loadMaps();
    }

    public void loadMaps() {
        maps.clear();
        if (!mapsFile.exists()) {
            plugin.saveResource("maps.yml", false);
        }
        mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);

        if (mapsConfig.contains("maps")) {
            for (String id : mapsConfig.getConfigurationSection("maps").getKeys(false)) {
                String path = "maps." + id + ".";
                String name = mapsConfig.getString(path + "name");
                int minPlayers = mapsConfig.getInt(path + "minPlayers");
                int maxPlayers = mapsConfig.getInt(path + "maxPlayers");
                com.bedwars.game.Map map = new com.bedwars.game.Map(id, name, minPlayers, maxPlayers);

                // Shopkeeper
                if (mapsConfig.contains(path + "shopkeeper")) {
                    map.setShopkeeperLocation(deserializeLocation(mapsConfig.getString(path + "shopkeeper")));
                }

                // Teams
                if (mapsConfig.contains(path + "teams")) {
                    for (String color : mapsConfig.getConfigurationSection(path + "teams").getKeys(false)) {
                        String tPath = path + "teams." + color + ".";
                        Location spawn = deserializeLocation(mapsConfig.getString(tPath + "spawn"));
                        Location bed = deserializeLocation(mapsConfig.getString(tPath + "bed"));
                        
                        if (spawn != null && bed != null) {
                            Team team = new Team(color, spawn, bed);
                            map.addTeam(team);
                            
                            // Also store in Map's separate storage for easy access
                            map.setSpawnPoint(color, spawn);
                            map.setBedLocation(color, bed);
                        }
                    }
                }
                maps.put(id, map);
            }
        }
    }

    public void saveMaps() {
        for (com.bedwars.game.Map map : maps.values()) {
            String path = "maps." + map.getId() + ".";
            mapsConfig.set(path + "name", map.getName());
            mapsConfig.set(path + "minPlayers", map.getMinPlayers());
            mapsConfig.set(path + "maxPlayers", map.getMaxPlayers());
            if (map.getShopkeeperLocation() != null)
                mapsConfig.set(path + "shopkeeper", serializeLocation(map.getShopkeeperLocation()));
            
            // Save teams with their spawn points and bed locations
            for (Team team : map.getTeams()) {
                String tPath = path + "teams." + team.getColor() + ".";
                if (team.getSpawn() != null)
                    mapsConfig.set(tPath + "spawn", serializeLocation(team.getSpawn()));
                if (team.getBed() != null)
                    mapsConfig.set(tPath + "bed", serializeLocation(team.getBed()));
            }
            
            // Also save spawn points and bed locations from the Map's separate storage
            for (String teamColor : new String[]{"red", "blue", "green", "yellow"}) {
                Location spawn = map.getSpawnPoint(teamColor);
                Location bed = map.getBedLocation(teamColor);
                
                if (spawn != null) {
                    String tPath = path + "teams." + teamColor + ".";
                    mapsConfig.set(tPath + "spawn", serializeLocation(spawn));
                }
                if (bed != null) {
                    String tPath = path + "teams." + teamColor + ".";
                    mapsConfig.set(tPath + "bed", serializeLocation(bed));
                }
            }
        }
        try {
            mapsConfig.save(mapsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save maps.yml: " + e.getMessage());
        }
    }

    public com.bedwars.game.Map getMap(String id) { return maps.get(id); }
    public Collection<com.bedwars.game.Map> getMaps() { return maps.values(); }
    public void addMap(com.bedwars.game.Map map) { maps.put(map.getId(), map); saveMaps(); }
    
    public void resetMap(String mapId) {
        maps.remove(mapId);
        if (mapsConfig.contains("maps." + mapId)) {
            mapsConfig.set("maps." + mapId, null);
            try {
                mapsConfig.save(mapsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save maps.yml: " + e.getMessage());
            }
        }
    }

    // Location serialization helpers
    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
    private Location deserializeLocation(String s) {
        if (s == null) return null;
        String[] parts = s.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(world, x, y, z);
    }
} 