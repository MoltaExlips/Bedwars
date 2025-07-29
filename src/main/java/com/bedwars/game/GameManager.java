package com.bedwars.game;

import com.bedwars.BedwarsPlugin;
import com.bedwars.utils.ConfigManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    private final java.util.Map<String, Game> games;
    private final java.util.Map<UUID, String> playerGames;
    
    public GameManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.games = new ConcurrentHashMap<>();
        this.playerGames = new ConcurrentHashMap<>();
    }
    
    public Game createGame(String mapId) {
        Map map = plugin.getMapManager().getMap(mapId);
        if (map == null) {
            return null;
        }
        
        String gameId = UUID.randomUUID().toString();
        Game game = new Game(gameId, map, plugin);
        games.put(gameId, game);
        
        return game;
    }
    
    public void removeGame(String gameId) {
        Game game = games.get(gameId);
        if (game != null) {
            // Remove all players from the game
            for (UUID playerId : game.getPlayers()) {
                playerGames.remove(playerId);
            }
            for (UUID playerId : game.getSpectators()) {
                playerGames.remove(playerId);
            }
            
            games.remove(gameId);
        }
    }
    
    public Game getGame(String gameId) {
        return games.get(gameId);
    }
    
    public Game getPlayerGame(UUID playerId) {
        String gameId = playerGames.get(playerId);
        return gameId != null ? games.get(gameId) : null;
    }
    
    public List<Game> getAvailableGames() {
        List<Game> availableGames = new ArrayList<>();
        for (Game game : games.values()) {
            if (game.getState() == GameState.WAITING && 
                game.getPlayers().size() < game.getMap().getMaxPlayers()) {
                availableGames.add(game);
            }
        }
        return availableGames;
    }
    
    public Game findAvailableGame(String mapId) {
        for (Game game : games.values()) {
            if (game.getState() == GameState.WAITING && 
                game.getMap().getId().equals(mapId) &&
                game.getPlayers().size() < game.getMap().getMaxPlayers()) {
                return game;
            }
        }
        return null;
    }
    
    public boolean joinGame(Player player, String mapId, String team) {
        // Check if player is already in a game
        if (getPlayerGame(player.getUniqueId()) != null) {
            return false;
        }
        
        // Find or create a game
        Game game = findAvailableGame(mapId);
        if (game == null) {
            game = createGame(mapId);
            if (game == null) {
                return false;
            }
        }
        
        // Add player to game
        game.addPlayer(player, team, null); // Pass null for kitId
        playerGames.put(player.getUniqueId(), game.getId());
        
        return true;
    }
    
    public void leaveGame(Player player) {
        Game game = getPlayerGame(player.getUniqueId());
        if (game != null) {
            game.removePlayer(player);
            playerGames.remove(player.getUniqueId());
            
            // If game is empty, remove it
            if (game.getPlayers().isEmpty() && game.getSpectators().isEmpty()) {
                removeGame(game.getId());
            }
        }
    }
    
    public void stopAllGames() {
        for (Game game : games.values()) {
            // Stop all generators
            for (Generator generator : game.getGenerators().values()) {
                generator.stop();
            }
        }
        games.clear();
        playerGames.clear();
    }
    
    public java.util.Map<String, Game> getGames() {
        return new HashMap<>(games);
    }
    
    public java.util.Map<UUID, String> getPlayerGames() {
        return new HashMap<>(playerGames);
    }
    
    public int getTotalGames() {
        return games.size();
    }
    
    public int getTotalPlayers() {
        return playerGames.size();
    }
} 