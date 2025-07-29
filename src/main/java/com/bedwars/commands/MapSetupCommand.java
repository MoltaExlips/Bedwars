package com.bedwars.commands;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.Map;
import com.bedwars.game.MapManager;
import com.bedwars.game.Team;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapSetupCommand implements CommandExecutor {
    private final BedwarsPlugin plugin;
    private final MapManager mapManager;

    public MapSetupCommand(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.mapManager = plugin.getMapManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("/bwmap <create|setshopkeeper|addteam|setteamspawn|setteambed|save|list|tp>");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("create") && args.length >= 5) {
            String id = args[1];
            String name = args[2];
            int min = Integer.parseInt(args[3]);
            int max = Integer.parseInt(args[4]);
            Map map = new Map(id, name, min, max);
            mapManager.addMap(map);
            player.sendMessage("Map created: " + id);
        } else if (sub.equals("setshopkeeper") && args.length >= 2) {
            Map map = mapManager.getMap(args[1]);
            if (map == null) { player.sendMessage("Map not found."); return true; }
            map.setShopkeeperLocation(player.getLocation());
            mapManager.saveMaps();
            player.sendMessage("Shopkeeper location set.");
        } else if (sub.equals("addteam") && args.length >= 3) {
            Map map = mapManager.getMap(args[1]);
            if (map == null) { player.sendMessage("Map not found."); return true; }
            String color = args[2];
            map.addTeam(new Team(color, null, null));
            mapManager.saveMaps();
            player.sendMessage("Team " + color + " added.");
        } else if (sub.equals("setteamspawn") && args.length >= 3) {
            Map map = mapManager.getMap(args[1]);
            if (map == null) { player.sendMessage("Map not found."); return true; }
            String color = args[2];
            for (Team t : map.getTeams()) {
                if (t.getColor().equalsIgnoreCase(color)) {
                    t.setSpawn(player.getLocation());
                    mapManager.saveMaps();
                    player.sendMessage("Spawn set for team " + color);
                    return true;
                }
            }
            player.sendMessage("Team not found.");
        } else if (sub.equals("setteambed") && args.length >= 3) {
            Map map = mapManager.getMap(args[1]);
            if (map == null) { player.sendMessage("Map not found."); return true; }
            String color = args[2];
            for (Team t : map.getTeams()) {
                if (t.getColor().equalsIgnoreCase(color)) {
                    t.setBed(player.getLocation());
                    mapManager.saveMaps();
                    player.sendMessage("Bed set for team " + color);
                    return true;
                }
            }
            player.sendMessage("Team not found.");
        } else if (sub.equals("save")) {
            mapManager.saveMaps();
            player.sendMessage("Maps saved.");
        } else if (sub.equals("list")) {
            for (Map m : mapManager.getMaps()) {
                player.sendMessage("- " + m.getId() + " (" + m.getName() + ")");
            }
        } else if (sub.equals("tp") && args.length >= 2) {
            Map map = mapManager.getMap(args[1]);
            if (map == null || map.getShopkeeperLocation() == null) {
                player.sendMessage("Map or shopkeeper not found.");
                return true;
            }
            player.teleport(map.getShopkeeperLocation());
        } else {
            player.sendMessage("Unknown or incomplete command.");
        }
        return true;
    }
} 