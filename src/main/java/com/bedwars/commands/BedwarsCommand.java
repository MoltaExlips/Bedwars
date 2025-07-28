package com.bedwars.commands;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Game;
import com.bedwars.utils.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class BedwarsCommand implements CommandExecutor {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    
    public BedwarsCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open main menu
            plugin.getLobbyManager().openMainMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "stats":
                handleStats(player);
                break;
            case "shop":
                handleShop(player);
                break;
            case "currency":
                handleCurrency(player);
                break;
            default:
                player.sendMessage("§cUnknown subcommand. Use /bedwars for help.");
                break;
        }
        
        return true;
    }
    
    private void handleJoin(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /bedwars join <map> <team> <kit>");
            return;
        }
        
        String mapId = args[1];
        String team = args[2];
        String kitId = args[3];
        
        // Check if map exists
        if (plugin.getMapManager().getMap(mapId) == null) {
            player.sendMessage("§cMap not found: " + mapId);
            return;
        }
        
        // Check if player is already in a game
        Game currentGame = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (currentGame != null) {
            player.sendMessage("§cYou are already in a game! Use /bedwars leave first.");
            return;
        }
        
        // Join the game
        boolean success = plugin.getGameManager().joinGame(player, mapId, team, kitId);
        if (success) {
            player.sendMessage(configManager.getMessage("lobby.joined"));
        } else {
            player.sendMessage("§cFailed to join game. The lobby might be full.");
        }
    }
    
    private void handleLeave(Player player) {
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game == null) {
            player.sendMessage("§cYou are not in a game!");
            return;
        }
        
        plugin.getGameManager().leaveGame(player);
        player.sendMessage(configManager.getMessage("lobby.left"));
        
        // Teleport to spawn
        player.teleport(player.getWorld().getSpawnLocation());
    }
    
    private void handleStats(Player player) {
        UUID playerId = player.getUniqueId();
        ConfigManager.PlayerStats stats = configManager.getPlayerStats(playerId);
        
        player.sendMessage("§8[§cBedwars§8] §6Your Statistics:");
        player.sendMessage("§7Wins: §a" + stats.getWins());
        player.sendMessage("§7Losses: §c" + stats.getLosses());
        player.sendMessage("§7Beds Destroyed: §e" + stats.getBedsDestroyed());
        player.sendMessage("§7Final Kills: §d" + stats.getFinalKills());
        
        int totalGames = stats.getWins() + stats.getLosses();
        if (totalGames > 0) {
            double winRate = (double) stats.getWins() / totalGames * 100;
            player.sendMessage("§7Win Rate: §6" + String.format("%.1f", winRate) + "%");
        }
    }
    
    private void handleShop(Player player) {
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game == null || game.getState() != com.bedwars.game.GameState.PLAYING) {
            player.sendMessage("§cYou must be in a game to use the shop!");
            return;
        }
        
        plugin.getShopManager().openShopCategory(player, "blocks");
    }
    
    private void handleCurrency(Player player) {
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game == null || game.getState() != com.bedwars.game.GameState.PLAYING) {
            player.sendMessage("§cYou must be in a game to check currency!");
            return;
        }
        
        plugin.getShopManager().displayPlayerCurrency(player);
    }
} 