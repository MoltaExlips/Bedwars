package com.bedwars.game;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Map {
    private final String id;
    private String name;
    private int minPlayers, maxPlayers;
    private final List<Team> teams = new ArrayList<>();
    private Location shopkeeperLocation;
    private World world;
    private boolean active;
    private String description;
    private final java.util.Map<String, List<Location>> generators = new HashMap<>();
    private final java.util.Map<String, Location> spawnPoints = new HashMap<>();
    private final java.util.Map<String, Location> bedLocations = new HashMap<>();

    public Map(String id, String name, int minPlayers, int maxPlayers) {
        this.id = id;
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.active = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public List<Team> getTeams() { return teams; }
    public void addTeam(Team team) { teams.add(team); }
    public Location getShopkeeperLocation() { return shopkeeperLocation; }
    public void setShopkeeperLocation(Location loc) { this.shopkeeperLocation = loc; }
    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.util.Map<String, List<Location>> getGenerators() { return generators; }
    public void addGenerator(String type, Location location) {
        generators.computeIfAbsent(type, k -> new ArrayList<>()).add(location);
    }

    public Location getSpawnPoint(String teamColor) { return spawnPoints.get(teamColor); }
    public void setSpawnPoint(String teamColor, Location location) { spawnPoints.put(teamColor, location); }

    public Location getBedLocation(String teamColor) { return bedLocations.get(teamColor); }
    public void setBedLocation(String teamColor, Location location) { bedLocations.put(teamColor, location); }

    public void resetWorld() {
        // Implementation for resetting the world
        if (world != null) {
            // Reset logic here - this would reset the map to its original state
            // For now, just a placeholder
        }
    }
} 