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
} 