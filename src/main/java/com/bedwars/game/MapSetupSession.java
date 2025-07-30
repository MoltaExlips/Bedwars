package com.bedwars.game;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class MapSetupSession {
    
    // Setup step enumeration for guided flow
    public enum SetupStep {
        REGION_SETUP("Set Map Region", "Define the map boundaries with Pos1 and Pos2"),
        TEAM_SETUP("Configure Teams", "Set up team spawns, beds, and shopkeepers"),
        MAP_SETTINGS("Map Settings", "Configure map name and player limits"),
        READY_TO_SAVE("Ready to Save", "Review and save your map");
        
        private final String displayName;
        private final String description;
        
        SetupStep(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    private final String mapId;
    private final World world;
    private String mapName;
    private int minPlayers;
    private int maxPlayers;
    
    private Location pos1;
    private Location pos2;
    private String currentTeam;
    private SetupStep currentStep;
    
    private final Map<String, Location> teamSpawns = new HashMap<>();
    private final Map<String, Location> teamBeds = new HashMap<>();
    private final Map<String, Location> teamShopkeepers = new HashMap<>();
    
    public MapSetupSession(String mapId, World world) {
        this.mapId = mapId;
        this.world = world;
        this.mapName = mapId.substring(0, 1).toUpperCase() + mapId.substring(1);
        this.minPlayers = 2;
        this.maxPlayers = 8;
        this.currentStep = SetupStep.REGION_SETUP;
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
    public SetupStep getCurrentStep() { return currentStep; }
    
    // Setters
    public void setMapName(String mapName) { this.mapName = mapName; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public void setCurrentTeam(String currentTeam) { this.currentTeam = currentTeam; }
    
    public void setPos1(Location pos1) { 
        this.pos1 = pos1; 
        updateStepProgress();
    }
    
    public void setPos2(Location pos2) { 
        this.pos2 = pos2; 
        updateStepProgress();
    }
    
    // Team management
    public void setTeamSpawn(String teamColor, Location location) {
        teamSpawns.put(teamColor, location);
        updateStepProgress();
    }
    
    public void setTeamBed(String teamColor, Location location) {
        teamBeds.put(teamColor, location);
        updateStepProgress();
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
    
    // Progress tracking methods
    public boolean isRegionComplete() {
        return pos1 != null && pos2 != null;
    }
    
    public boolean areTeamsComplete() {
        return getTeamCount() >= 2; // At least 2 teams required
    }
    
    public int getProgressPercentage() {
        int totalSteps = 4;
        int completedSteps = 0;
        
        if (isRegionComplete()) completedSteps++;
        if (areTeamsComplete()) completedSteps++;
        if (mapName != null && !mapName.isEmpty()) completedSteps++;
        if (isComplete()) completedSteps++;
        
        return (completedSteps * 100) / totalSteps;
    }
    
    public String getNextStepHint() {
        switch (currentStep) {
            case REGION_SETUP:
                if (pos1 == null) return "Right-click a block to set Position 1";
                if (pos2 == null) return "Left-click a block to set Position 2";
                return "Region setup complete! Moving to team setup...";
            case TEAM_SETUP:
                if (getTeamCount() == 0) return "Click a team color to start configuring teams";
                if (getTeamCount() < 2) return "Set up at least 2 teams for the map";
                return "Teams setup complete! Configure map settings...";
            case MAP_SETTINGS:
                return "Adjust map name and player limits, then save";
            case READY_TO_SAVE:
                return "Click 'Save Map' to finish setup";
            default:
                return "";
        }
    }
    
    private void updateStepProgress() {
        switch (currentStep) {
            case REGION_SETUP:
                if (isRegionComplete()) {
                    currentStep = SetupStep.TEAM_SETUP;
                }
                break;
            case TEAM_SETUP:
                if (areTeamsComplete()) {
                    currentStep = SetupStep.MAP_SETTINGS;
                }
                break;
            case MAP_SETTINGS:
                if (isComplete()) {
                    currentStep = SetupStep.READY_TO_SAVE;
                }
                break;
        }
    }
    
    public void advanceToNextStep() {
        SetupStep[] steps = SetupStep.values();
        int currentIndex = currentStep.ordinal();
        if (currentIndex < steps.length - 1) {
            currentStep = steps[currentIndex + 1];
        }
    }
} 