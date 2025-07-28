package com.bedwars.game;

import com.bedwars.BedwarsPlugin;
import com.bedwars.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
    
    private final String id;
    private final com.bedwars.game.Map map;
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    
    private GameState state;
    private final java.util.Map<String, Team> teams;
    private final Set<UUID> players;
    private final Set<UUID> spectators;
    private final java.util.Map<UUID, String> playerTeams;
    private final java.util.Map<UUID, Kit> playerKits;
    private final java.util.Map<UUID, Integer> respawnTasks;
    
    private BukkitTask countdownTask;
    private BukkitTask gameTask;
    private int countdownSeconds;
    private int gameTimeSeconds;
    private final int maxGameTime;
    
    private final java.util.Map<String, Generator> generators;
    
    public Game(String id, com.bedwars.game.Map map, BedwarsPlugin plugin) {
        this.id = id;
        this.map = map;
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        
        this.state = GameState.WAITING;
        this.teams = new HashMap<>();
        this.players = ConcurrentHashMap.newKeySet();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.playerTeams = new ConcurrentHashMap<>();
        this.playerKits = new ConcurrentHashMap<>();
        this.respawnTasks = new ConcurrentHashMap<>();
        
        this.countdownSeconds = configManager.getCountdownSeconds();
        this.maxGameTime = configManager.getGameDurationMinutes() * 60;
        this.gameTimeSeconds = 0;
        
        this.generators = new HashMap<>();
        
        setupTeams();
        setupGenerators();
    }
    
    private void setupTeams() {
        // Create teams based on map configuration
        String[] teamColors = {"red", "blue", "green", "yellow"};
        Material[] bedMaterials = {Material.RED_BED, Material.BLUE_BED, Material.GREEN_BED, Material.YELLOW_BED};
        
        for (int i = 0; i < map.getTeams(); i++) {
            String color = teamColors[i];
            Location spawnLoc = map.getSpawnPoint(color);
            Location bedLoc = map.getBedLocation(color);
            
            if (spawnLoc != null && bedLoc != null) {
                Team team = new Team(color, color, bedMaterials[i], spawnLoc, bedLoc);
                teams.put(color, team);
            }
        }
    }
    
    private void setupGenerators() {
        java.util.Map<String, List<Location>> generatorLocations = map.getGenerators();
        java.util.Map<String, ConfigManager.GeneratorConfig> generatorConfigs = configManager.getGenerators();
        
        for (java.util.Map.Entry<String, List<Location>> entry : generatorLocations.entrySet()) {
            String type = entry.getKey();
            List<Location> locations = entry.getValue();
            ConfigManager.GeneratorConfig config = generatorConfigs.get(type);
            
            if (config != null) {
                for (Location location : locations) {
                    Generator generator = new Generator(location, config.getMaterial(), config.getIntervalSeconds(), config.getMaxItems());
                    generators.put(type + "_" + location.hashCode(), generator);
                }
            }
        }
    }
    
    public void addPlayer(Player player, String team, String kitId) {
        if (state != GameState.WAITING) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Team teamObj = teams.get(team);
        
        if (teamObj != null && !teamObj.hasPlayer(playerId)) {
            players.add(playerId);
            playerTeams.put(playerId, team);
            teamObj.addPlayer(playerId);
            
            // Set kit
            java.util.Map<String, Kit> kits = configManager.getKits();
            Kit kit = kits.get(kitId);
            if (kit != null) {
                playerKits.put(playerId, kit);
            }
            
            // Teleport to spawn
            player.teleport(teamObj.getSpawnLocation());
            
            // Give kit items
            if (kit != null) {
                kit.giveItems(player);
            }
            
            // Check if we can start the game
            if (players.size() >= configManager.getMinPlayers()) {
                startCountdown();
            }
        }
    }
    
    public void removePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (players.contains(playerId)) {
            players.remove(playerId);
            String teamName = playerTeams.remove(playerId);
            playerKits.remove(playerId);
            
            if (teamName != null) {
                Team team = teams.get(teamName);
                if (team != null) {
                    team.removePlayer(playerId);
                }
            }
            
            // Cancel respawn task if exists
            Integer taskId = respawnTasks.remove(playerId);
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            
            // Check if game should end
            if (state == GameState.PLAYING && getAliveTeams().size() <= 1) {
                endGame();
            }
        } else if (spectators.contains(playerId)) {
            spectators.remove(playerId);
        }
    }
    
    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        player.teleport(map.getWorld().getSpawnLocation());
    }
    
    private void startCountdown() {
        if (state != GameState.WAITING) {
            return;
        }
        
        state = GameState.COUNTDOWN;
        countdownSeconds = configManager.getCountdownSeconds();
        
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds > 0) {
                    // Broadcast countdown
                    for (UUID playerId : players) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null) {
                            java.util.Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("time", String.valueOf(countdownSeconds));
                            player.sendMessage(configManager.getMessage("lobby.countdown", placeholders));
                        }
                    }
                    countdownSeconds--;
                } else {
                    startGame();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void startGame() {
        state = GameState.PLAYING;
        gameTimeSeconds = 0;
        
        // Start generators
        for (Generator generator : generators.values()) {
            generator.start();
        }
        
        // Start game timer
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                gameTimeSeconds++;
                
                // Check for game end conditions
                if (gameTimeSeconds >= maxGameTime) {
                    endGame();
                    return;
                }
                
                // Check if only one team remains
                List<Team> aliveTeams = getAliveTeams();
                if (aliveTeams.size() <= 1) {
                    endGame();
                    return;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        
        // Broadcast game start
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(configManager.getMessage("game.started"));
            }
        }
    }
    
    private void endGame() {
        state = GameState.ENDING;
        
        // Stop tasks
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (gameTask != null) {
            gameTask.cancel();
        }
        
        // Stop generators
        for (Generator generator : generators.values()) {
            generator.stop();
        }
        
        // Determine winner
        List<Team> aliveTeams = getAliveTeams();
        String winner = "No one";
        if (aliveTeams.size() == 1) {
            winner = aliveTeams.get(0).getName();
        }
        
        // Broadcast game end
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                java.util.Map<String, String> placeholders = new HashMap<>();
                placeholders.put("winner", winner);
                player.sendMessage(configManager.getMessage("game.ended", placeholders));
            }
        }
        
        // Reset map after delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            map.resetWorld();
            state = GameState.FINISHED;
        }, 100L);
    }
    
    public void destroyBed(String teamName) {
        Team team = teams.get(teamName);
        if (team != null && !team.isBedDestroyed()) {
            team.setBedDestroyed(true);
            
            // Broadcast bed destruction
            for (UUID playerId : players) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    java.util.Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("team", teamName);
                    player.sendMessage(configManager.getMessage("game.bed-destroyed", placeholders));
                }
            }
            
            // Check if team is eliminated
            if (team.getPlayerCount() == 0) {
                team.setEliminated(true);
            }
        }
    }
    
    public void killPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        String teamName = playerTeams.get(playerId);
        
        if (teamName != null) {
            Team team = teams.get(teamName);
            if (team != null && team.isBedDestroyed()) {
                // Final kill - player is eliminated
                players.remove(playerId);
                playerTeams.remove(playerId);
                team.removePlayer(playerId);
                
                // Broadcast elimination
                for (UUID otherPlayerId : players) {
                    Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayer != null) {
                        java.util.Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", player.getName());
                        otherPlayer.sendMessage(configManager.getMessage("game.player-eliminated", placeholders));
                    }
                }
                
                // Check if team is eliminated
                if (team.getPlayerCount() == 0) {
                    team.setEliminated(true);
                }
            } else {
                // Respawn player
                int respawnTime = configManager.getRespawnTimeSeconds();
                java.util.Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(respawnTime));
                player.sendMessage(configManager.getMessage("game.respawn", placeholders));
                
                // Schedule respawn
                BukkitTask respawnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (players.contains(playerId)) {
                        player.teleport(team.getSpawnLocation());
                        respawnTasks.remove(playerId);
                    }
                }, respawnTime * 20L);
                
                respawnTasks.put(playerId, respawnTask.getTaskId());
            }
        }
    }
    
    public List<Team> getAliveTeams() {
        List<Team> aliveTeams = new ArrayList<>();
        for (Team team : teams.values()) {
            if (team.isAlive()) {
                aliveTeams.add(team);
            }
        }
        return aliveTeams;
    }
    
    // Getters
    public String getId() { return id; }
    public com.bedwars.game.Map getMap() { return map; }
    public GameState getState() { return state; }
    public Set<UUID> getPlayers() { return new HashSet<>(players); }
    public Set<UUID> getSpectators() { return new HashSet<>(spectators); }
    public java.util.Map<String, Team> getTeams() { return new HashMap<>(teams); }
    public java.util.Map<UUID, String> getPlayerTeams() { return new HashMap<>(playerTeams); }
    public java.util.Map<UUID, Kit> getPlayerKits() { return new HashMap<>(playerKits); }
    public java.util.Map<String, Generator> getGenerators() { return new HashMap<>(generators); }
    
    public Team getPlayerTeam(UUID playerId) {
        String teamName = playerTeams.get(playerId);
        return teamName != null ? teams.get(teamName) : null;
    }
    
    public Kit getPlayerKit(UUID playerId) {
        return playerKits.get(playerId);
    }
    
    public boolean isPlayerInGame(UUID playerId) {
        return players.contains(playerId) || spectators.contains(playerId);
    }
    
    public boolean isPlayerAlive(UUID playerId) {
        return players.contains(playerId);
    }
} 