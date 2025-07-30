package com.bedwars.game;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
    private final String color;
    private Location spawn;
    private Location bed;
    private final Set<UUID> players = new HashSet<>();
    private boolean eliminated = false;
    private boolean bedDestroyed = false;

    public Team(String color, Location spawn, Location bed) {
        this.color = color;
        this.spawn = spawn;
        this.bed = bed;
    }

    public String getColor() { return color; }
    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }
    public Location getBed() { return bed; }
    public void setBed(Location bed) { this.bed = bed; }

    public Set<UUID> getPlayers() {
        return players;
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

    public String getName() {
        return color;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public Location getSpawnLocation() {
        return spawn;
    }

    public Location getBedLocation() {
        return bed;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public boolean isBedDestroyed() {
        return bedDestroyed;
    }

    public void setBedDestroyed(boolean bedDestroyed) {
        this.bedDestroyed = bedDestroyed;
    }
} 