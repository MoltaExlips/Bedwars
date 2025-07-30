package com.bedwars.game;

import com.bedwars.BedwarsPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MapSetupManager {
    
    private final BedwarsPlugin plugin;
    private final java.util.Map<String, MapSetupSession> activeSessions = new HashMap<>();
    
    public MapSetupManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Map Setup");
        
        // Create Map button
        ItemStack createItem = createMenuItem(Material.GRASS_BLOCK, "§aCreate New Map", 
            "§7Start creating a new Bedwars map", "§7Click to begin");
        inv.setItem(11, createItem);
        
        // Edit Map button
        ItemStack editItem = createMenuItem(Material.ANVIL, "§eEdit Existing Map", 
            "§7Modify an existing map", "§7Click to select");
        inv.setItem(13, editItem);
        
        // List Maps button
        ItemStack listItem = createMenuItem(Material.BOOK, "§6List All Maps", 
            "§7View all available maps", "§7Click to view");
        inv.setItem(15, listItem);
        
        player.openInventory(inv);
    }
    
    public void startMapCreation(Player player, String mapId) {
        if (getSession(player) != null) {
            player.sendMessage("§cYou are already in map setup mode! Use /mapsetup save or /mapsetup cancel first.");
            return;
        }
        if (plugin.getMapManager().getMap(mapId) != null) {
            player.sendMessage("§cA map with that ID already exists!");
            return;
        }
        
        // Create a new void world
        WorldCreator creator = new WorldCreator("bedwars_" + mapId);
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);
        
        try {
            World world = creator.createWorld();
            if (world == null) {
                player.sendMessage("§cFailed to create world!");
                return;
            }
            
            // Clear the world to make it void-like
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int x = -100; x <= 100; x++) {
                        for (int z = -100; z <= 100; z++) {
                            for (int y = 0; y <= 10; y++) {
                                world.getBlockAt(x, y, z).setType(Material.AIR);
                            }
                        }
                    }
                    player.sendMessage("§aWorld created You can now start setting up your map.");
                }
            }.runTaskLater(plugin, 20L);
            
            // Create setup session
            MapSetupSession session = new MapSetupSession(mapId, world);
            activeSessions.put(player.getUniqueId().toString(), session);
            
            // Teleport player to the new world
            Location spawnLoc = new Location(world, 0, 64, 0);
            player.teleport(spawnLoc);
            
            // Start the setup process
            startSetupTutorial(player, session);
            
        } catch (Exception e) {
            player.sendMessage("§cFailed to create world: " + e.getMessage());
            plugin.getLogger().severe("Failed to create world for map " + mapId + ": " + e.getMessage());
        }
    }
    
    private void startSetupTutorial(Player player, MapSetupSession session) {
        player.sendMessage("§8[§cBedwars§8] §6Map Setup Tutorial Started!");
        player.sendMessage("§7Step 1: Set the map region (Pos1 and Pos2)");
        player.sendMessage("§7Use §e/mapsetup pos1 §7and §e/mapsetup pos2 §7to set the corners");
        player.sendMessage("§7This defines the area that will be reset between games");
        
        // Give the player a setup wand
        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§6Map Setup Wand");
        meta.setLore(Arrays.asList("§7Right-click to set positions", "§7Left-click to set team spawns"));
        wand.setItemMeta(meta);
        
        player.getInventory().addItem(wand);
    }
    
    public void setPos1(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        session.setPos1(player.getLocation());
        player.sendMessage("§aPosition 1 set at: " + formatLocation(player.getLocation()));
        
        if (session.getPos2() != null) {
            player.sendMessage("§7Step 2: Set up teams and spawns");
            openTeamSetupMenu(player);
        }
    }
    
    public void setPos2(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        session.setPos2(player.getLocation());
        player.sendMessage("§aPosition 2 set at: " + formatLocation(player.getLocation()));
        
        if (session.getPos1() != null) {
            player.sendMessage("§7Step 2: Set up teams and spawns");
            openTeamSetupMenu(player);
        }
    }
    
    public void openTeamSetupMenu(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) return;
        
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBedwars§8] Team Setup");
        
        // Team colors
        String[] colors = {"red", "blue", "green", "yellow"};
        Material[] materials = {Material.RED_WOOL, Material.BLUE_WOOL, Material.LIME_WOOL, Material.YELLOW_WOOL};
        
        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            boolean hasTeam = session.hasTeam(color);
            
            ItemStack item = new ItemStack(hasTeam ? Material.EMERALD : materials[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§" + getTeamColorCode(color) + color.toUpperCase() + " TEAM");
            
            List<String> lore = new ArrayList<>();
            if (hasTeam) {
                lore.add("§a✓ Team configured");
                lore.add("§7Spawn: " + formatLocation(session.getTeamSpawn(color)));
                lore.add("§7Bed: " + formatLocation(session.getTeamBed(color)));
                lore.add("§7Shopkeeper: " + formatLocation(session.getTeamShopkeeper(color)));
            } else {
                lore.add("§c✗ Not configured");
                lore.add("§7Click to set up this team");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(i, item);
        }
        
        // Continue button
        if (session.getTeamCount() > 0) {
            ItemStack continueItem = createMenuItem(Material.EMERALD_BLOCK, "§aContinue to Map Settings", 
                "§7Configure map name, min/max players", "§7Click to continue");
            inv.setItem(31, continueItem);
        }
        
        player.openInventory(inv);
    }
    
    public void setupTeam(Player player, String teamColor) {
        MapSetupSession session = getSession(player);
        if (session == null) return;
        
        session.setCurrentTeam(teamColor);
        player.sendMessage("§7Setting up §" + getTeamColorCode(teamColor) + teamColor + " §7team");
        player.sendMessage("§7Right-click where you want the spawn point");
        player.sendMessage("§7Left-click where you want the bed");
        player.sendMessage("§7Use §e/mapsetup shopkeeper §7to set shopkeeper location");
    }
    
    public void setTeamSpawn(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null || session.getCurrentTeam() == null) {
            player.sendMessage("§cYou need to select a team first!");
            return;
        }
        
        session.setTeamSpawn(session.getCurrentTeam(), player.getLocation());
        player.sendMessage("§a" + session.getCurrentTeam() + " team spawn set!");
    }
    
    public void setTeamBed(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null || session.getCurrentTeam() == null) {
            player.sendMessage("§cYou need to select a team first!");
            return;
        }
        
        session.setTeamBed(session.getCurrentTeam(), player.getLocation());
        player.sendMessage("§a" + session.getCurrentTeam() + " team bed set!");
    }
    
    public void setTeamShopkeeper(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null || session.getCurrentTeam() == null) {
            player.sendMessage("§cYou need to select a team first!");
            return;
        }
        
        session.setTeamShopkeeper(session.getCurrentTeam(), player.getLocation());
        player.sendMessage("§a" + session.getCurrentTeam() + " team shopkeeper set!");
        
        // Spawn the shopkeeper
        plugin.getShopkeeperManager().spawnShopkeeper(session.getCurrentTeam(), player.getLocation());
    }
    
    public void editMapName(Player player, String newName) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        session.setMapName(newName);
        player.sendMessage("§aMap name changed to: " + newName);
    }
    
    public void setMinPlayers(Player player, int minPlayers) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        if (minPlayers < 2 || minPlayers > 16) {
            player.sendMessage("§cMin players must be between 2 and 16!");
            return;
        }
        
        session.setMinPlayers(minPlayers);
        player.sendMessage("§aMin players set to: " + minPlayers);
    }
    
    public void setMaxPlayers(Player player, int maxPlayers) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        if (maxPlayers < 2 || maxPlayers > 16) {
            player.sendMessage("§cMax players must be between 2 and 16!");
            return;
        }
        
        if (maxPlayers < session.getMinPlayers()) {
            player.sendMessage("§cMax players cannot be less than min players!");
            return;
        }
        
        session.setMaxPlayers(maxPlayers);
        player.sendMessage("§aMax players set to: " + maxPlayers);
    }
    
    public void openMapSettingsMenu(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) return;
        
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Map Settings");
        
        // Map name
        ItemStack nameItem = createMenuItem(Material.NAME_TAG, "§6Map Name: " + session.getMapName(), 
            "§7Current: " + session.getMapName(), "§7Click to change");
        inv.setItem(10, nameItem);
        
        // Min players
        ItemStack minItem = createMenuItem(Material.IRON_INGOT, "§eMin Players: " + session.getMinPlayers(), 
            "§7Current: " + session.getMinPlayers(), "§7Click to change");
        inv.setItem(12, minItem);
        
        // Max players
        ItemStack maxItem = createMenuItem(Material.GOLD_INGOT, "§6Max Players: " + session.getMaxPlayers(), 
            "§7Current: " + session.getMaxPlayers(), "§7Click to change");
        inv.setItem(14, maxItem);
        
        // Save map
        ItemStack saveItem = createMenuItem(Material.EMERALD_BLOCK, "§aSave Map", 
            "§7Save the map and finish setup", "§7Click to save");
        inv.setItem(16, saveItem);
        
        player.openInventory(inv);
    }
    
    public void saveMap(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) return;
        
        // Create the map
        Map map = new Map(session.getMapId(), session.getMapName(), session.getMinPlayers(), session.getMaxPlayers());
        map.setWorld(session.getWorld());
        
        // Add teams
        for (String teamColor : session.getTeamColors()) {
            Location spawn = session.getTeamSpawn(teamColor);
            Location bed = session.getTeamBed(teamColor);
            Location shopkeeper = session.getTeamShopkeeper(teamColor);
            
            if (spawn != null && bed != null) {
                Team team = new Team(teamColor, spawn, bed);
                map.addTeam(team);
                map.setSpawnPoint(teamColor, spawn);
                map.setBedLocation(teamColor, bed);
                if (shopkeeper != null) {
                    map.setShopkeeperLocation(shopkeeper);
                }
            }
        }
        
        // Save the map
        plugin.getMapManager().addMap(map);
        
        player.sendMessage("§aMap '" + session.getMapName() + "' saved successfully!");
        player.sendMessage("§7Map ID: " + session.getMapId());
        player.sendMessage("§7Teams: " + session.getTeamCount());
        player.sendMessage("§7Players: " + session.getMinPlayers() + "-" + session.getMaxPlayers());
        
        // Clean up
        activeSessions.remove(player.getUniqueId().toString());
        player.getInventory().clear();
        
        // Teleport back to spawn
        player.teleport(player.getServer().getWorlds().get(0).getSpawnLocation());
    }
    
    public MapSetupSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId().toString());
    }
    
    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    
    private String getTeamColorCode(String color) {
        switch (color.toLowerCase()) {
            case "red": return "c";
            case "blue": return "9";
            case "green": return "a";
            case "yellow": return "e";
            default: return "f";
        }
    }
    
    private String formatLocation(Location loc) {
        if (loc == null) return "Not set";
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    public void editMap(Player player, String mapId) {
        if (getSession(player) != null) {
            player.sendMessage("§cYou are already in map setup mode! Use /mapsetup save or /mapsetup cancel first.");
            return;
        }
        Map map = plugin.getMapManager().getMap(mapId);
        if (map == null) {
            player.sendMessage("§cMap not found: " + mapId);
            return;
        }
        
        // Create a setup session with existing map data
        MapSetupSession session = new MapSetupSession(mapId, map.getWorld());
        session.setMapName(map.getName());
        session.setMinPlayers(map.getMinPlayers());
        session.setMaxPlayers(map.getMaxPlayers());
        // Load teams
        for (Team team : map.getTeams()) {
            String color = team.getColor();
            session.setTeamSpawn(color, team.getSpawn());
            session.setTeamBed(color, team.getBed());
            // Shopkeeper location if available
            if (map.getShopkeeperLocation() != null) {
                session.setTeamShopkeeper(color, map.getShopkeeperLocation());
            }
        }
        activeSessions.put(player.getUniqueId().toString(), session);
        // Teleport player to the map world
        player.teleport(map.getWorld().getSpawnLocation());
        // Start the team setup GUI/tutorial for editing
        openTeamSetupMenu(player);
        player.sendMessage("§aEditing map: " + map.getName());
    }
    
    public void showMapList(Player player) {
        Collection<Map> maps = plugin.getMapManager().getMaps();
        if (maps.isEmpty()) {
            player.sendMessage("§cNo maps found!");
            return;
        }
        
        player.sendMessage("§8[§cBedwars§8] §6Available Maps:");
        for (Map map : maps) {
            player.sendMessage("§7- §e" + map.getId() + " §7(§f" + map.getName() + "§7)");
        }
    }
    
    public void deleteMap(Player player, String mapId) {
        Map map = plugin.getMapManager().getMap(mapId);
        if (map == null) {
            player.sendMessage("§cMap not found: " + mapId);
            return;
        }
        
        plugin.getMapManager().resetMap(mapId);
        player.sendMessage("§aMap deleted: " + mapId);
    }

    public void cancelMapSetup(Player player) {
        if (getSession(player) != null) {
            activeSessions.remove(player.getUniqueId().toString());
            player.sendMessage("§cExited map setup mode.");
            player.getInventory().clear();
            player.teleport(player.getServer().getWorlds().get(0).getSpawnLocation());
        } else {
            player.sendMessage("§cYou are not in map setup mode.");
        }
    }
} 