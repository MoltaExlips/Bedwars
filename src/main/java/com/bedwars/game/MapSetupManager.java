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
        sendProgressUpdate(player, session);
        
        // Give the player a setup wand with dynamic lore
        giveSetupWand(player, session);
        
        // Open the main setup GUI to guide the process
        openSetupProgressGUI(player, session);
    }
    
    private void sendProgressUpdate(Player player, MapSetupSession session) {
        player.sendMessage("§8[§cBedwars§8] §6Map Setup Progress");
        player.sendMessage("§7Map: §e" + session.getMapName() + " §8(§7" + session.getProgressPercentage() + "%§8)");
        player.sendMessage("§7Current Step: §a" + session.getCurrentStep().getDisplayName());
        player.sendMessage("§7" + session.getCurrentStep().getDescription());
        player.sendMessage("§8▶ §e" + session.getNextStepHint());
        player.sendMessage("§8" + "=".repeat(40));
    }
    
    private void giveSetupWand(Player player, MapSetupSession session) {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§6✦ Map Setup Wand ✦");
        
        List<String> lore = new ArrayList<>();
        lore.add("§8" + "▬".repeat(25));
        lore.add("§7Current Step: §a" + session.getCurrentStep().getDisplayName());
        lore.add("§8");
        
        switch (session.getCurrentStep()) {
            case REGION_SETUP:
                lore.add("§e§l» §7Right-click: §aSet Position 1");
                lore.add("§e§l» §7Left-click: §aSet Position 2");
                lore.add("§8");
                lore.add("§7Define the map boundaries that will");
                lore.add("§7be reset between games");
                break;
            case TEAM_SETUP:
                if (session.getCurrentTeam() != null) {
                    lore.add("§e§l» §7Right-click: §aSet " + session.getCurrentTeam() + " spawn");
                    lore.add("§e§l» §7Place bed: §aSet " + session.getCurrentTeam() + " bed");
                    lore.add("§7  §8(or left-click with wand)");
                    lore.add("§8");
                    lore.add("§7Setting up: §" + getTeamColorCode(session.getCurrentTeam()) + session.getCurrentTeam().toUpperCase() + " TEAM");
                } else {
                    lore.add("§c§l» §7Open team menu to select a team first");
                    lore.add("§7Use: §e/mapsetup teams");
                }
                break;
            default:
                lore.add("§7Use §e/mapsetup teams §7to configure teams");
                lore.add("§7Use §e/mapsetup settings §7for map settings");
                break;
        }
        
        lore.add("§8" + "▬".repeat(25));
        meta.setLore(lore);
        wand.setItemMeta(meta);
        
        // Clear inventory first and give wand
        player.getInventory().clear();
        player.getInventory().setItem(0, wand);
        
        // Give navigation items
        ItemStack menuItem = new ItemStack(Material.COMPASS);
        ItemMeta menuMeta = menuItem.getItemMeta();
        menuMeta.setDisplayName("§6Setup Menu");
        menuMeta.setLore(Arrays.asList("§7Right-click to open setup progress"));
        menuItem.setItemMeta(menuMeta);
        player.getInventory().setItem(8, menuItem);
    }
    
    public void setPos1(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        session.setPos1(player.getLocation());
        player.sendMessage("§a✓ Position 1 set at: " + formatLocation(player.getLocation()));
        
        // Update wand and provide next step guidance
        giveSetupWand(player, session);
        
        if (session.isRegionComplete()) {
            player.sendMessage("§a§l✓ Region setup complete!");
            sendProgressUpdate(player, session);
            
            // Auto-advance to team setup after a brief delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    openTeamSetupMenu(player);
                }
            }.runTaskLater(plugin, 40L); // 2 second delay
        } else {
            player.sendMessage("§7Next: " + session.getNextStepHint());
        }
    }
    
    public void setPos2(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in map setup mode!");
            return;
        }
        
        session.setPos2(player.getLocation());
        player.sendMessage("§a✓ Position 2 set at: " + formatLocation(player.getLocation()));
        
        // Update wand and provide next step guidance
        giveSetupWand(player, session);
        
        if (session.isRegionComplete()) {
            player.sendMessage("§a§l✓ Region setup complete!");
            sendProgressUpdate(player, session);
            
            // Auto-advance to team setup after a brief delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    openTeamSetupMenu(player);
                }
            }.runTaskLater(plugin, 40L); // 2 second delay
        } else {
            player.sendMessage("§7Next: " + session.getNextStepHint());
        }
    }
    
    public void openSetupProgressGUI(Player player, MapSetupSession session) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Setup Progress");
        
        // Progress indicator
        int progress = session.getProgressPercentage();
        ItemStack progressItem = createMenuItem(Material.EXPERIENCE_BOTTLE, 
            "§6Setup Progress: " + progress + "%",
            "§7Current Step: §a" + session.getCurrentStep().getDisplayName(),
            "§8" + session.getCurrentStep().getDescription(),
            "§8",
            "§e" + session.getNextStepHint());
        inv.setItem(4, progressItem);
        
        // Step indicators
        MapSetupSession.SetupStep[] steps = MapSetupSession.SetupStep.values();
        for (int i = 0; i < steps.length; i++) {
            MapSetupSession.SetupStep step = steps[i];
            boolean isComplete = false;
            boolean isCurrent = session.getCurrentStep() == step;
            
            // Check completion status
            switch (step) {
                case REGION_SETUP:
                    isComplete = session.isRegionComplete();
                    break;
                case TEAM_SETUP:
                    isComplete = session.areTeamsComplete();
                    break;
                case MAP_SETTINGS:
                    isComplete = session.getMapName() != null && !session.getMapName().isEmpty();
                    break;
                case READY_TO_SAVE:
                    isComplete = session.isComplete();
                    break;
            }
            
            Material material = isComplete ? Material.EMERALD : (isCurrent ? Material.GOLD_INGOT : Material.GRAY_DYE);
            String status = isComplete ? "§a✓ Complete" : (isCurrent ? "§e▶ Current" : "§7○ Pending");
            
            ItemStack stepItem = createMenuItem(material, 
                status + " §f" + step.getDisplayName(),
                "§8" + step.getDescription());
            inv.setItem(9 + i * 2, stepItem);
        }
        
        // Quick action buttons
        ItemStack teamButton = createMenuItem(Material.IRON_SWORD, "§6Configure Teams", 
            "§7Set up team spawns and beds", "§7Click to open team menu");
        inv.setItem(18, teamButton);
        
        ItemStack settingsButton = createMenuItem(Material.NAME_TAG, "§6Map Settings", 
            "§7Configure map name and limits", "§7Click to open settings");
        inv.setItem(20, settingsButton);
        
        if (session.isComplete()) {
            ItemStack saveButton = createMenuItem(Material.EMERALD_BLOCK, "§a§lSave Map", 
                "§7Finish setup and save the map", "§7Click to save");
            inv.setItem(22, saveButton);
        }
        
        player.openInventory(inv);
    }
    
    public void openTeamSetupMenu(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null) return;
        
        Inventory inv = Bukkit.createInventory(null, 45, "§8[§cBedwars§8] Team Setup");
        
        // Progress indicator at top
        int progress = session.getProgressPercentage();
        ItemStack progressItem = createMenuItem(Material.EXPERIENCE_BOTTLE, 
            "§6Setup Progress: " + progress + "%",
            "§7Step: §a" + session.getCurrentStep().getDisplayName(),
            "§e" + session.getNextStepHint());
        inv.setItem(4, progressItem);
        
        // Team colors
        String[] colors = {"red", "blue", "green", "yellow"};
        Material[] materials = {Material.RED_WOOL, Material.BLUE_WOOL, Material.LIME_WOOL, Material.YELLOW_WOOL};
        
        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            boolean hasTeam = session.hasTeam(color);
            boolean isSelected = color.equals(session.getCurrentTeam());
            
            Material itemMaterial = hasTeam ? Material.EMERALD_BLOCK : 
                                   (isSelected ? Material.GOLD_BLOCK : materials[i]);
            
            ItemStack item = new ItemStack(itemMaterial);
            ItemMeta meta = item.getItemMeta();
            
            String prefix = hasTeam ? "§a✓ " : (isSelected ? "§e▶ " : "§7○ ");
            meta.setDisplayName(prefix + "§" + getTeamColorCode(color) + color.toUpperCase() + " TEAM");
            
            List<String> lore = new ArrayList<>();
            if (hasTeam) {
                lore.add("§a§l✓ FULLY CONFIGURED");
                lore.add("§8");
                lore.add("§7Spawn: " + formatLocation(session.getTeamSpawn(color)));
                lore.add("§7Bed: " + formatLocation(session.getTeamBed(color)));
                if (session.getTeamShopkeeper(color) != null) {
                    lore.add("§7Shopkeeper: " + formatLocation(session.getTeamShopkeeper(color)));
                }
                lore.add("§8");
                lore.add("§7Click to reconfigure this team");
            } else if (isSelected) {
                lore.add("§e§l▶ CURRENTLY SELECTED");
                lore.add("§8");
                lore.add("§7Use your wand to set:");
                lore.add("§a• Right-click: Set spawn point");
                lore.add("§a• Left-click: Set bed location");
                lore.add("§7Use §e/mapsetup shopkeeper §7for shopkeeper");
            } else {
                lore.add("§c§l✗ NOT CONFIGURED");
                lore.add("§8");
                lore.add("§7Click to select and configure");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(19 + i * 2, item);
        }
        
        // Navigation buttons
        ItemStack backButton = createMenuItem(Material.ARROW, "§7« Back to Progress", 
            "§7Return to main setup screen");
        inv.setItem(36, backButton);
        
        // Continue button
        if (session.areTeamsComplete()) {
            ItemStack continueItem = createMenuItem(Material.EMERALD_BLOCK, "§a§lContinue to Settings »", 
                "§7Configure map name and player limits", "§7Click to continue");
            inv.setItem(44, continueItem);
        } else {
            ItemStack hintItem = createMenuItem(Material.BARRIER, "§c§lNeed More Teams", 
                "§7Set up at least 2 teams to continue",
                "§7Teams configured: " + session.getTeamCount() + "/4");
            inv.setItem(44, hintItem);
        }
        
        player.openInventory(inv);
    }
    
    public void setupTeam(Player player, String teamColor) {
        MapSetupSession session = getSession(player);
        if (session == null) return;
        
        session.setCurrentTeam(teamColor);
        player.closeInventory();
        
        player.sendMessage("§8[§cBedwars§8] §6Team Setup");
        player.sendMessage("§7Selected: §" + getTeamColorCode(teamColor) + teamColor.toUpperCase() + " TEAM");
        player.sendMessage("§8");
        player.sendMessage("§a§l» §7Right-click with wand: Set spawn point");
        player.sendMessage("§a§l» §7Place a bed: Set bed location §8(easier!)");
        player.sendMessage("§7  §8Alternative: Left-click with wand");
        player.sendMessage("§a§l» §7Use §e/mapsetup shopkeeper §7to set shopkeeper");
        player.sendMessage("§8" + "=".repeat(40));
        
        // Update the wand to reflect current team
        giveSetupWand(player, session);
    }
    
    public void setTeamSpawn(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null || session.getCurrentTeam() == null) {
            player.sendMessage("§cYou need to select a team first! Use §e/mapsetup teams");
            return;
        }
        
        String team = session.getCurrentTeam();
        session.setTeamSpawn(team, player.getLocation());
        player.sendMessage("§a✓ " + team.toUpperCase() + " team spawn set at: " + formatLocation(player.getLocation()));
        
        // Check if team is now complete
        if (session.hasTeam(team)) {
            player.sendMessage("§a§l✓ " + team.toUpperCase() + " team fully configured!");
            
            // If we have enough teams, suggest moving to next step
            if (session.areTeamsComplete()) {
                player.sendMessage("§7Tip: You have enough teams! Use §e/mapsetup settings §7to continue");
            }
        } else {
            player.sendMessage("§7Next: Set the bed location with left-click");
        }
        
        // Update wand
        giveSetupWand(player, session);
    }
    
    public void setTeamBed(Player player) {
        MapSetupSession session = getSession(player);
        if (session == null || session.getCurrentTeam() == null) {
            player.sendMessage("§cYou need to select a team first! Use §e/mapsetup teams");
            return;
        }
        
        String team = session.getCurrentTeam();
        session.setTeamBed(team, player.getLocation());
        player.sendMessage("§a✓ " + team.toUpperCase() + " team bed set at: " + formatLocation(player.getLocation()));
        
        // Check if team is now complete
        if (session.hasTeam(team)) {
            player.sendMessage("§a§l✓ " + team.toUpperCase() + " team fully configured!");
            
            // If we have enough teams, suggest moving to next step
            if (session.areTeamsComplete()) {
                player.sendMessage("§7Tip: You have enough teams! Use §e/mapsetup settings §7to continue");
            }
        } else {
            player.sendMessage("§7Next: Set the spawn point with right-click");
        }
        
        // Update wand
        giveSetupWand(player, session);
    }
    
    public void setTeamBedFromBlock(Player player, Location bedLocation) {
        MapSetupSession session = getSession(player);
        if (session == null || session.getCurrentTeam() == null) {
            return;
        }
        
        String team = session.getCurrentTeam();
        session.setTeamBed(team, bedLocation);
        
        // Check if team is now complete
        if (session.hasTeam(team)) {
            player.sendMessage("§a§l✓ " + team.toUpperCase() + " team fully configured!");
            
            // If we have enough teams, suggest moving to next step
            if (session.areTeamsComplete()) {
                player.sendMessage("§7Tip: You have enough teams! Use §e/mapsetup settings §7to continue");
            }
        } else {
            player.sendMessage("§7Next: Set the spawn point with right-click on your wand");
        }
        
        // Update wand to reflect progress
        giveSetupWand(player, session);
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