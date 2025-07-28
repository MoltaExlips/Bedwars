package com.bedwars;

import com.bedwars.commands.BedwarsCommand;
import com.bedwars.commands.BedwarsAdminCommand;
import com.bedwars.game.GameManager;
import com.bedwars.game.LobbyManager;
import com.bedwars.game.MapManager;
import com.bedwars.listeners.*;
import com.bedwars.shop.ShopManager;
import com.bedwars.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsPlugin extends JavaPlugin {
    
    private static BedwarsPlugin instance;
    private ConfigManager configManager;
    private GameManager gameManager;
    private LobbyManager lobbyManager;
    private MapManager mapManager;
    private ShopManager shopManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        gameManager = new GameManager(this);
        lobbyManager = new LobbyManager(this);
        mapManager = new MapManager(this);
        shopManager = new ShopManager(this);
        
        // Register commands
        getCommand("bedwars").setExecutor(new BedwarsCommand(this));
        getCommand("bedwarsadmin").setExecutor(new BedwarsAdminCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new GeneratorListener(this), this);
        getServer().getPluginManager().registerEvents(new BedListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Load maps
        mapManager.loadMaps();
        
        getLogger().info("Bedwars plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Stop all games
        if (gameManager != null) {
            gameManager.stopAllGames();
        }
        
        // Save data
        if (configManager != null) {
            configManager.saveData();
        }
        
        getLogger().info("Bedwars plugin has been disabled!");
    }
    
    public static BedwarsPlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }
    
    public MapManager getMapManager() {
        return mapManager;
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
} 