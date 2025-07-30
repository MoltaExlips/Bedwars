package com.bedwars.game;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class MapSetupSession {
    
    private final String mapId;
    private final World world;
    private String mapName;
    private int minPlayers;
    private int maxPlayers;
    
    private Location pos1;
    private Location pos2;
    private String currentTeam;
    
    private final Map<String, Location> teamSpawns = new HashMap<>();
    private final Map<String, Location> teamBeds = new HashMap<>();
    private final Map<String, Location> teamShopkeepers = new HashMap<>();
    
    public MapSetupSession(String mapId, World world) {
        this.mapId = mapId;
        this.world = world;
        this.mapName = mapId.substring(0, 1).toUpperCase() + mapId.substring(1);
        this.minPlayers = 2;
        this.maxPlayers = 8;
    }
    
    // Getters
    public String getMapId() { return mapId; }
    public World getWorld() { return world; }
    public String getMapName() { return mapName; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }
    public String getCurrentTeam() { return currentTeam; }
    
    // Setters
    public void setMapName(String mapName) { this.mapName = mapName; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
    public void setCurrentTeam(String currentTeam) { this.currentTeam = currentTeam; }
    
    // Team management
    public void setTeamSpawn(String teamColor, Location location) {
        teamSpawns.put(teamColor, location);
    }
    
    public void setTeamBed(String teamColor, Location location) {
        teamBeds.put(teamColor, location);
    }
    
    public void setTeamShopkeeper(String teamColor, Location location) {
        teamShopkeepers.put(teamColor, location);
    }
    
    public Location getTeamSpawn(String teamColor) {
        return teamSpawns.get(teamColor);
    }
    
    public Location getTeamBed(String teamColor) {
        return teamBeds.get(teamColor);
    }
    
    public Location getTeamShopkeeper(String teamColor) {
        return teamShopkeepers.get(teamColor);
    }
    
    public boolean hasTeam(String teamColor) {
        return teamSpawns.containsKey(teamColor) && teamBeds.containsKey(teamColor);
    }
    
    public int getTeamCount() {
        int count = 0;
        for (String color : new String[]{"red", "blue", "green", "yellow"}) {
            if (hasTeam(color)) count++;
        }
        return count;
    }
    
    public String[] getTeamColors() {
        return new String[]{"red", "blue", "green", "yellow"};
    }
    
    public boolean isComplete() {
        return pos1 != null && pos2 != null && getTeamCount() > 0;
    }
} 