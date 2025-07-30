package com.bedwars.commands;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.MapSetupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapSetupCommand implements CommandExecutor {
    
    private final BedwarsPlugin plugin;
    private final MapSetupManager setupManager;
    
    public MapSetupCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.setupManager = new MapSetupManager(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (!sender.hasPermission("bedwars.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§8[§cBedwars§8] §6Map Setup Help:");
            player.sendMessage("§e/mapsetup create <id> §7- Create a new map");
            player.sendMessage("§e/mapsetup edit <id> §7- Edit an existing map");
            player.sendMessage("§e/mapsetup list §7- List all maps");
            player.sendMessage("§e/mapsetup delete <id> §7- Delete a map");
            player.sendMessage("§e/mapsetup pos1 §7- Set region position 1");
            player.sendMessage("§e/mapsetup pos2 §7- Set region position 2");
            player.sendMessage("§e/mapsetup teams §7- Configure teams");
            player.sendMessage("§e/mapsetup settings §7- Map settings (name, players)");
            player.sendMessage("§e/mapsetup save §7- Save the map");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mapsetup create <mapId>");
                    return true;
                }
                setupManager.startMapCreation(player, args[1]);
                break;
            case "edit":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mapsetup edit <mapId>");
                    return true;
                }
                setupManager.editMap(player, args[1]);
                break;
            case "list":
                setupManager.showMapList(player);
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mapsetup delete <mapId>");
                    return true;
                }
                setupManager.deleteMap(player, args[1]);
                break;
            case "pos1":
                setupManager.setPos1(player);
                break;
            case "pos2":
                setupManager.setPos2(player);
                break;
            case "spawn":
                setupManager.setTeamSpawn(player);
                break;
            case "bed":
                setupManager.setTeamBed(player);
                break;
            case "shopkeeper":
                setupManager.setTeamShopkeeper(player);
                break;
            case "teams":
                setupManager.openTeamSetupMenu(player);
                break;
            case "settings":
                setupManager.openMapSettingsMenu(player);
                break;
            case "save":
                setupManager.saveMap(player);
                break;
            case "name":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mapsetup name <newName>");
                    return true;
                }
                setupManager.editMapName(player, args[1]);
                break;
            case "minplayers":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mapsetup minplayers <number>");
                    return true;
                }
                try {
                    int minPlayers = Integer.parseInt(args[1]);
                    setupManager.setMinPlayers(player, minPlayers);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cPlease provide a valid number!");
                }
                break;
            case "maxplayers":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /mapsetup maxplayers <number>");
                    return true;
                }
                try {
                    int maxPlayers = Integer.parseInt(args[1]);
                    setupManager.setMaxPlayers(player, maxPlayers);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cPlease provide a valid number!");
                }
                break;
            case "cancel":
                setupManager.cancelMapSetup(player);
                break;
            default:
                player.sendMessage("§cUsage: /mapsetup <create|edit|list|delete|pos1|pos2|spawn|bed|shopkeeper|teams|settings|save|name|minplayers|maxplayers> [mapId]");
                break;
        }
        
        return true;
    }
} 