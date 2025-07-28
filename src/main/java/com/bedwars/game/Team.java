package com.bedwars.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
    
    private final String name;
    private final String color;
    private final Material bedMaterial;
    private final Location spawnLocation;
    private final Location bedLocation;
    private final Set<UUID> players;
    private boolean bedDestroyed;
    private boolean eliminated;
    
    public Team(String name, String color, Material bedMaterial, Location spawnLocation, Location bedLocation) {
        this.name = name;
        this.color = color;
        this.bedMaterial = bedMaterial;
        this.spawnLocation = spawnLocation;
        this.bedLocation = bedLocation;
        this.players = new HashSet<>();
        this.bedDestroyed = false;
        this.eliminated = false;
    }
    
    public String getName() {
        return name;
    }
    
    public String getColor() {
        return color;
    }
    
    public Material getBedMaterial() {
        return bedMaterial;
    }
    
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    public Location getBedLocation() {
        return bedLocation;
    }
    
    public Set<UUID> getPlayers() {
        return new HashSet<>(players);
    }
    
    public void addPlayer(UUID playerId) {
        players.add(playerId);
    }
    
    public void removePlayer(UUID playerId) {
        players.remove(playerId);
    }
    
    public boolean hasPlayer(UUID playerId) {
        return players.contains(playerId);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean isBedDestroyed() {
        return bedDestroyed;
    }
    
    public void setBedDestroyed(boolean bedDestroyed) {
        this.bedDestroyed = bedDestroyed;
    }
    
    public boolean isEliminated() {
        return eliminated;
    }
    
    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
    
    public boolean isAlive() {
        return !eliminated && !bedDestroyed;
    }
    
    public void clearPlayers() {
        players.clear();
    }
    
    public void reset() {
        bedDestroyed = false;
        eliminated = false;
        players.clear();
    }
} 