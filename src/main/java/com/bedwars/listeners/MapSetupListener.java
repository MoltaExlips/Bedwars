package com.bedwars.listeners;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.MapSetupManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.bedwars.game.MapSetupSession;
import org.bukkit.ChatColor;

public class MapSetupListener implements Listener {
    
    private final BedwarsPlugin plugin;
    private final MapSetupManager setupManager;
    private final Map<UUID, String> waitingForInput = new HashMap<>();
    
    public MapSetupListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.setupManager = plugin.getMapSetupManager();
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (waitingForInput.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String inputType = waitingForInput.remove(player.getUniqueId());
            
            switch (inputType) {
                case "mapname":
                    setupManager.editMapName(player, message);
                    break;
                case "minplayers":
                    try {
                        int minPlayers = Integer.parseInt(message);
                        setupManager.setMinPlayers(player, minPlayers);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cPlease provide a valid number!");
                    }
                    break;
                case "maxplayers":
                    try {
                        int maxPlayers = Integer.parseInt(message);
                        setupManager.setMaxPlayers(player, maxPlayers);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cPlease provide a valid number!");
                    }
                    break;
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // Check if player is using the setup wand
        if (displayName.contains("Map Setup Wand")) {
            event.setCancelled(true);
            
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                // Check if a team is currently selected
                MapSetupSession session = setupManager.getSession(player);
                if (session != null && session.getCurrentTeam() != null) {
                    // Set spawn point for current team
                    setupManager.setTeamSpawn(player);
                } else {
                    // Set Pos1 for map region
                    setupManager.setPos1(player);
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                // Check if a team is currently selected
                MapSetupSession session = setupManager.getSession(player);
                if (session != null && session.getCurrentTeam() != null) {
                    // Set bed location for current team
                    setupManager.setTeamBed(player);
                } else {
                    // Set Pos2 for map region
                    setupManager.setPos2(player);
                }
            }
        }
        
        // Check if player is using the compass (setup menu)
        if (displayName.contains("Setup Menu")) {
            event.setCancelled(true);
            
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                MapSetupSession session = setupManager.getSession(player);
                if (session != null) {
                    // Open the setup progress GUI
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            setupManager.openSetupProgressGUI(player, session);
                        }
                    }.runTask(plugin);
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Only handle our setup GUIs
        if (!(title.contains("Map Setup") || title.contains("Team Setup") || title.contains("Map Settings") || title.contains("Setup Progress"))) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        // Setup progress menu
        if (title.contains("Setup Progress")) {
            handleSetupProgressClick(player, clicked);
            return;
        }
        
        // Main setup menu
        if (title.contains("Map Setup") && !title.contains("Team Setup") && !title.contains("Setup Progress")) {
            handleMainMenuClick(player, clicked);
            return;
        }
        
        // Team setup menu
        if (title.contains("Team Setup")) {
            handleTeamSetupClick(player, clicked);
            return;
        }
        
        // Map settings menu
        if (title.contains("Map Settings")) {
            handleMapSettingsClick(player, clicked);
            return;
        }
    }
    
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        setupManager.cancelMapSetup(player);
    }
    
    private void handleMainMenuClick(Player player, ItemStack clicked) {
        switch (clicked.getType()) {
            case GRASS_BLOCK:
                // Create new map - this should be handled by command
                player.sendMessage("§cUse /mapsetup create <mapId> to start creating a map");
                break;
            case ANVIL:
                // Edit existing map
                player.sendMessage("§cUse /mapsetup edit <mapId> to edit a map");
                break;
            case BOOK:
                // List maps
                setupManager.showMapList(player);
                break;
        }
    }
    
    private void handleSetupProgressClick(Player player, ItemStack clicked) {
        switch (clicked.getType()) {
            case IRON_SWORD:
                // Configure Teams button
                setupManager.openTeamSetupMenu(player);
                break;
            case NAME_TAG:
                // Map Settings button
                setupManager.openMapSettingsMenu(player);
                break;
            case EMERALD_BLOCK:
                // Save Map button
                setupManager.saveMap(player);
                break;
        }
    }
    
    private void handleTeamSetupClick(Player player, ItemStack clicked) {
        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toUpperCase();
        
        // Navigation buttons
        if (clicked.getType() == Material.ARROW) {
            // Back to progress
            MapSetupSession session = setupManager.getSession(player);
            if (session != null) {
                setupManager.openSetupProgressGUI(player, session);
            }
            return;
        }
        
        // Check for team colors
        if (displayName.contains("RED TEAM")) {
            setupManager.setupTeam(player, "red");
        } else if (displayName.contains("BLUE TEAM")) {
            setupManager.setupTeam(player, "blue");
        } else if (displayName.contains("GREEN TEAM")) {
            setupManager.setupTeam(player, "green");
        } else if (displayName.contains("YELLOW TEAM")) {
            setupManager.setupTeam(player, "yellow");
        } else if (clicked.getType() == Material.EMERALD_BLOCK && displayName.contains("CONTINUE")) {
            // Continue to map settings
            setupManager.openMapSettingsMenu(player);
        }
    }
    
    private void handleMapSettingsClick(Player player, ItemStack clicked) {
        switch (clicked.getType()) {
            case NAME_TAG:
                // Change map name - prompt player for input
                player.sendMessage("§6Please type the new map name in chat:");
                player.sendMessage("§7Or use: /mapsetup name <newName>");
                waitingForInput.put(player.getUniqueId(), "mapname");
                break;
            case IRON_INGOT:
                // Change min players - prompt player for input
                player.sendMessage("§6Please type the new minimum players in chat:");
                player.sendMessage("§7Or use: /mapsetup minplayers <number>");
                waitingForInput.put(player.getUniqueId(), "minplayers");
                break;
            case GOLD_INGOT:
                // Change max players - prompt player for input
                player.sendMessage("§6Please type the new maximum players in chat:");
                player.sendMessage("§7Or use: /mapsetup maxplayers <number>");
                waitingForInput.put(player.getUniqueId(), "maxplayers");
                break;
            case EMERALD_BLOCK:
                // Save map
                setupManager.saveMap(player);
                break;
        }
    }
} 