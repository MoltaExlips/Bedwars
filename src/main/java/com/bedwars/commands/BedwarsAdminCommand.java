package com.bedwars.commands;

import com.bedwars.BedwarsPlugin;
import com.bedwars.utils.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedwarsAdminCommand implements CommandExecutor {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    
    public BedwarsAdminCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bedwars.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /bedwarsadmin <create|delete|reload|setup>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "setup":
                handleSetup(sender, args);
                break;
            case "stats":
                handleStats(sender);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand. Use /bedwarsadmin for help.");
                break;
        }
        
        return true;
    }
    
    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /bedwarsadmin create <mapId>");
            return;
        }
        
        String mapId = args[1];
        
        // Check if map already exists
        if (plugin.getMapManager().getMap(mapId) != null) {
            sender.sendMessage("§cMap already exists: " + mapId);
            return;
        }
        
        // Use the new map setup system
        plugin.getMapSetupManager().startMapCreation(player, mapId);
    }
    
    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /bedwarsadmin delete <mapId>");
            return;
        }
        
        String mapId = args[1];
        
        // Check if map exists
        if (plugin.getMapManager().getMap(mapId) == null) {
            sender.sendMessage("§cMap not found: " + mapId);
            return;
        }
        
        // Delete the map
        plugin.getMapManager().resetMap(mapId);
        sender.sendMessage(configManager.getMessage("admin.map-deleted", java.util.Map.of("map", mapId)));
    }
    
    private void handleReload(CommandSender sender) {
        configManager.reloadConfig();
        plugin.getMapManager().loadMaps();
        sender.sendMessage(configManager.getMessage("admin.config-reloaded"));
    }
    
    private void handleSetup(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /bedwarsadmin setup <mapId>");
            return;
        }
        
        String mapId = args[1];
        
        // Check if map exists
        if (plugin.getMapManager().getMap(mapId) == null) {
            sender.sendMessage("§cMap not found: " + mapId);
            return;
        }
        
        // Setup mode for map configuration
        sender.sendMessage("§aSetup mode is not yet implemented. Please configure maps in config.yml");
    }
    
    private void handleStats(CommandSender sender) {
        int totalGames = plugin.getGameManager().getTotalGames();
        int totalPlayers = plugin.getGameManager().getTotalPlayers();
        
        sender.sendMessage("§8[§cBedwars§8] §6Server Statistics:");
        sender.sendMessage("§7Active Games: §e" + totalGames);
        sender.sendMessage("§7Total Players: §e" + totalPlayers);
        sender.sendMessage("§7Loaded Maps: §e" + plugin.getMapManager().getMaps().size());
    }
} 