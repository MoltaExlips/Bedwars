package com.bedwars.utils;

import com.bedwars.BedwarsPlugin;
import com.bedwars.game.GameState;
import com.bedwars.game.Kit;
import com.bedwars.game.Map;
import com.bedwars.game.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {
    
    private final BedwarsPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration data;
    private File dataFile;
    
    public ConfigManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadData();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }
    
    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // Game Settings
    public int getMinPlayers() {
        return config.getInt("game.min-players", 2);
    }
    
    public int getMaxPlayers() {
        return config.getInt("game.max-players", 8);
    }
    
    public int getCountdownSeconds() {
        return config.getInt("game.countdown-seconds", 30);
    }
    
    public int getGameDurationMinutes() {
        return config.getInt("game.game-duration-minutes", 15);
    }
    
    public int getBedDestroyTimeSeconds() {
        return config.getInt("game.bed-destroy-time-seconds", 5);
    }
    
    public int getRespawnTimeSeconds() {
        return config.getInt("game.respawn-time-seconds", 3);
    }
    
    // Kits
    public java.util.Map<String, Kit> getKits() {
        java.util.Map<String, Kit> kits = new HashMap<>();
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        
        if (kitsSection != null) {
            for (String kitId : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitId);
                if (kitSection != null) {
                    String name = kitSection.getString("name", kitId);
                    String description = kitSection.getString("description", "");
                    List<ItemStack> items = new ArrayList<>();
                    
                    ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");
                    if (itemsSection != null) {
                        for (String itemId : itemsSection.getKeys(false)) {
                            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
                            if (itemSection != null) {
                                Material material = Material.valueOf(itemSection.getString("material", "STONE"));
                                int amount = itemSection.getInt("amount", 1);
                                items.add(new ItemStack(material, amount));
                            }
                        }
                    }
                    
                    kits.put(kitId, new Kit(kitId, name, description, items));
                }
            }
        }
        
        return kits;
    }
    
    // Generators
    public java.util.Map<String, GeneratorConfig> getGenerators() {
        java.util.Map<String, GeneratorConfig> generators = new HashMap<>();
        ConfigurationSection generatorsSection = config.getConfigurationSection("generators");
        
        if (generatorsSection != null) {
            for (String generatorId : generatorsSection.getKeys(false)) {
                ConfigurationSection generatorSection = generatorsSection.getConfigurationSection(generatorId);
                if (generatorSection != null) {
                    int interval = generatorSection.getInt("interval-seconds", 2);
                    int maxItems = generatorSection.getInt("max-items", 8);
                    Material material = Material.valueOf(generatorSection.getString("material", "IRON_INGOT"));
                    
                    generators.put(generatorId, new GeneratorConfig(interval, maxItems, material));
                }
            }
        }
        
        return generators;
    }
    
    // Shop Items
    public java.util.Map<String, ShopItem> getShopItems() {
        java.util.Map<String, ShopItem> items = new HashMap<>();
        ConfigurationSection shopSection = config.getConfigurationSection("shop");
        
        if (shopSection != null) {
            for (String category : shopSection.getKeys(false)) {
                ConfigurationSection categorySection = shopSection.getConfigurationSection(category);
                if (categorySection != null) {
                    for (String itemId : categorySection.getKeys(false)) {
                        ConfigurationSection itemSection = categorySection.getConfigurationSection(itemId);
                        if (itemSection != null) {
                            int cost = itemSection.getInt("cost", 1);
                            Material currency = Material.valueOf(itemSection.getString("currency", "IRON_INGOT"));
                            Material material = Material.valueOf(itemSection.getString("material", "STONE"));
                            int amount = itemSection.getInt("amount", 1);
                            
                            items.put(itemId, new ShopItem(itemId, cost, currency, material, amount));
                        }
                    }
                }
            }
        }
        
        return items;
    }
    
    // Maps
    public java.util.Map<String, com.bedwars.game.Map> getMaps() {
        java.util.Map<String, com.bedwars.game.Map> maps = new HashMap<>();
        ConfigurationSection mapsSection = config.getConfigurationSection("maps");
        
        if (mapsSection != null) {
            for (String mapId : mapsSection.getKeys(false)) {
                ConfigurationSection mapSection = mapsSection.getConfigurationSection(mapId);
                if (mapSection != null) {
                    String name = mapSection.getString("name", mapId);
                    String description = mapSection.getString("description", "");
                    int minPlayers = mapSection.getInt("min-players", 2);
                    int maxPlayers = mapSection.getInt("max-players", 8);
                    int teams = mapSection.getInt("teams", 4);
                    
                    java.util.Map<String, Location> spawnPoints = new HashMap<>();
                    java.util.Map<String, Location> bedLocations = new HashMap<>();
                    java.util.Map<String, List<Location>> generators = new HashMap<>();
                    
                    // Load spawn points
                    ConfigurationSection spawnSection = mapSection.getConfigurationSection("spawn-points");
                    if (spawnSection != null) {
                        for (String team : spawnSection.getKeys(false)) {
                            String locStr = spawnSection.getString(team);
                            spawnPoints.put(team, parseLocation(locStr));
                        }
                    }
                    
                    // Load bed locations
                    ConfigurationSection bedSection = mapSection.getConfigurationSection("bed-locations");
                    if (bedSection != null) {
                        for (String team : bedSection.getKeys(false)) {
                            String locStr = bedSection.getString(team);
                            bedLocations.put(team, parseLocation(locStr));
                        }
                    }
                    
                    // Load generators
                    ConfigurationSection generatorsSection = mapSection.getConfigurationSection("generators");
                    if (generatorsSection != null) {
                        for (String generatorType : generatorsSection.getKeys(false)) {
                            List<String> locStrs = generatorsSection.getStringList(generatorType);
                            List<Location> locations = new ArrayList<>();
                            for (String locStr : locStrs) {
                                locations.add(parseLocation(locStr));
                            }
                            generators.put(generatorType, locations);
                        }
                    }
                    
                    maps.put(mapId, new Map(mapId, name, description, minPlayers, maxPlayers, teams, spawnPoints, bedLocations, generators));
                }
            }
        }
        
        return maps;
    }
    
    // Messages
    public String getMessage(String path) {
        return config.getString("messages." + path, "Message not found: " + path);
    }
    
    public String getMessage(String path, java.util.Map<String, String> placeholders) {
        String message = getMessage(path);
        for (java.util.Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
    
    // Helper methods
    private Location parseLocation(String locStr) {
        String[] parts = locStr.split(",");
        if (parts.length >= 3) {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            return new Location(plugin.getServer().getWorlds().get(0), x, y, z);
        }
        return null;
    }
    
    // Data storage
    public void savePlayerStats(UUID playerId, PlayerStats stats) {
        data.set("stats." + playerId.toString() + ".wins", stats.getWins());
        data.set("stats." + playerId.toString() + ".losses", stats.getLosses());
        data.set("stats." + playerId.toString() + ".beds-destroyed", stats.getBedsDestroyed());
        data.set("stats." + playerId.toString() + ".final-kills", stats.getFinalKills());
        saveData();
    }
    
    public PlayerStats getPlayerStats(UUID playerId) {
        String path = "stats." + playerId.toString();
        int wins = data.getInt(path + ".wins", 0);
        int losses = data.getInt(path + ".losses", 0);
        int bedsDestroyed = data.getInt(path + ".beds-destroyed", 0);
        int finalKills = data.getInt(path + ".final-kills", 0);
        return new PlayerStats(wins, losses, bedsDestroyed, finalKills);
    }
    
    // Inner classes
    public static class GeneratorConfig {
        private final int intervalSeconds;
        private final int maxItems;
        private final Material material;
        
        public GeneratorConfig(int intervalSeconds, int maxItems, Material material) {
            this.intervalSeconds = intervalSeconds;
            this.maxItems = maxItems;
            this.material = material;
        }
        
        public int getIntervalSeconds() { return intervalSeconds; }
        public int getMaxItems() { return maxItems; }
        public Material getMaterial() { return material; }
    }
    
    public static class ShopItem {
        private final String id;
        private final int cost;
        private final Material currency;
        private final Material material;
        private final int amount;
        
        public ShopItem(String id, int cost, Material currency, Material material, int amount) {
            this.id = id;
            this.cost = cost;
            this.currency = currency;
            this.material = material;
            this.amount = amount;
        }
        
        public String getId() { return id; }
        public int getCost() { return cost; }
        public Material getCurrency() { return currency; }
        public Material getMaterial() { return material; }
        public int getAmount() { return amount; }
    }
    
    public static class PlayerStats {
        private int wins;
        private int losses;
        private int bedsDestroyed;
        private int finalKills;
        
        public PlayerStats(int wins, int losses, int bedsDestroyed, int finalKills) {
            this.wins = wins;
            this.losses = losses;
            this.bedsDestroyed = bedsDestroyed;
            this.finalKills = finalKills;
        }
        
        public int getWins() { return wins; }
        public void setWins(int wins) { this.wins = wins; }
        public int getLosses() { return losses; }
        public void setLosses(int losses) { this.losses = losses; }
        public int getBedsDestroyed() { return bedsDestroyed; }
        public void setBedsDestroyed(int bedsDestroyed) { this.bedsDestroyed = bedsDestroyed; }
        public int getFinalKills() { return finalKills; }
        public void setFinalKills(int finalKills) { this.finalKills = finalKills; }
    }
} 