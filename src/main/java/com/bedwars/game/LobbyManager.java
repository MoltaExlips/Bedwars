package com.bedwars.game;

import com.bedwars.BedwarsPlugin;
import com.bedwars.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;
import com.bedwars.game.Map;
import java.util.List;

public class LobbyManager {
    
    private final BedwarsPlugin plugin;
    private final ConfigManager configManager;
    private final java.util.Map<UUID, String> playerSelectedMap;
    private final java.util.Map<UUID, String> playerSelectedTeam;
    
    public LobbyManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerSelectedMap = new HashMap<>();
        this.playerSelectedTeam = new HashMap<>();
    }
    
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8[§cBedwars§8] Main Menu");
        
        // Join Game button
        ItemStack joinItem = new ItemStack(Material.EMERALD);
        ItemMeta joinMeta = joinItem.getItemMeta();
        joinMeta.setDisplayName("§aJoin Game");
        joinMeta.setLore(java.util.Arrays.asList("§7Click to join a Bedwars game"));
        joinItem.setItemMeta(joinMeta);
        inv.setItem(11, joinItem);
        
        // Leave Game button
        ItemStack leaveItem = new ItemStack(Material.REDSTONE);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§cLeave Game");
        leaveMeta.setLore(java.util.Arrays.asList("§7Click to leave your current game"));
        leaveItem.setItemMeta(leaveMeta);
        inv.setItem(15, leaveItem);
        
        player.openInventory(inv);
    }
    
    public void openMapSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8[§cBedwars§8] Select Map");
        
        int slot = 0;
        for (Map mapObj : plugin.getMapManager().getMaps()) {
            String id = mapObj.getId();
            
            ItemStack mapItem = new ItemStack(Material.MAP);
            ItemMeta mapMeta = mapItem.getItemMeta();
            mapMeta.setDisplayName("§6" + mapObj.getName());
            mapMeta.setLore(java.util.Arrays.asList(
                "§7" + mapObj.getDescription(),
                "§7Players: §e" + mapObj.getMinPlayers() + "§7-§e" + mapObj.getMaxPlayers(),
                "§7Teams: §e" + mapObj.getTeams(),
                "§7Click to select this map"
            ));
            mapItem.setItemMeta(mapMeta);
            
            inv.setItem(slot, mapItem);
            slot++;
        }
        
        player.openInventory(inv);
    }
    
    public void openTeamSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "§8[§cBedwars§8] Select Team");
        
        String selectedMap = playerSelectedMap.get(player.getUniqueId());
        if (selectedMap != null) {
            com.bedwars.game.Map map = plugin.getMapManager().getMap(selectedMap);
            if (map != null) {
                List<String> teamList = map.getTeams().stream()
                    .map(Team::getColor)
                    .collect(java.util.stream.Collectors.toList());
                Material[] teamMaterials = {Material.RED_WOOL, Material.BLUE_WOOL, Material.LIME_WOOL, Material.YELLOW_WOOL};

                for (int i = 0; i < teamList.size() && i < teamMaterials.length; i++) {
                    String teamName = teamList.get(i);
                    ItemStack teamItem = new ItemStack(teamMaterials[i]);
                    ItemMeta teamMeta = teamItem.getItemMeta();
                    teamMeta.setDisplayName("§" + getTeamColor(teamName) + teamName.toUpperCase());
                    teamMeta.setLore(java.util.Arrays.asList("§7Click to join this team"));
                    teamItem.setItemMeta(teamMeta);
                    
                    inv.setItem(i, teamItem);
                }
            }
        }
        
        player.openInventory(inv);
    }

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8[§cBedwars§8] Shop");
        
        // Blocks section
        ItemStack blocksItem = new ItemStack(Material.WHITE_WOOL);
        ItemMeta blocksMeta = blocksItem.getItemMeta();
        blocksMeta.setDisplayName("§aBlocks");
        blocksMeta.setLore(java.util.Arrays.asList("§7Click to view blocks"));
        blocksItem.setItemMeta(blocksMeta);
        inv.setItem(10, blocksItem);
        
        // Weapons section
        ItemStack weaponsItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta weaponsMeta = weaponsItem.getItemMeta();
        weaponsMeta.setDisplayName("§cWeapons");
        weaponsMeta.setLore(java.util.Arrays.asList("§7Click to view weapons"));
        weaponsItem.setItemMeta(weaponsMeta);
        inv.setItem(12, weaponsItem);
        
        // Armor section
        ItemStack armorItem = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta armorMeta = armorItem.getItemMeta();
        armorMeta.setDisplayName("§9Armor");
        armorMeta.setLore(java.util.Arrays.asList("§7Click to view armor"));
        armorItem.setItemMeta(armorMeta);
        inv.setItem(14, armorItem);
        
        // Tools section
        ItemStack toolsItem = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta toolsMeta = toolsItem.getItemMeta();
        toolsMeta.setDisplayName("§6Tools");
        toolsMeta.setLore(java.util.Arrays.asList("§7Click to view tools"));
        toolsItem.setItemMeta(toolsMeta);
        inv.setItem(16, toolsItem);
        
        // Utilities section
        ItemStack utilitiesItem = new ItemStack(Material.TNT);
        ItemMeta utilitiesMeta = utilitiesItem.getItemMeta();
        utilitiesMeta.setDisplayName("§5Utilities");
        utilitiesMeta.setLore(java.util.Arrays.asList("§7Click to view utilities"));
        utilitiesItem.setItemMeta(utilitiesMeta);
        inv.setItem(28, utilitiesItem);
        
        player.openInventory(inv);
    }
    
    private String getTeamColor(String team) {
        switch (team.toLowerCase()) {
            case "red": return "c";
            case "blue": return "9";
            case "green": return "a";
            case "yellow": return "e";
            default: return "f";
        }
    }
    
    public void setPlayerSelection(UUID playerId, String type, String value) {
        switch (type.toLowerCase()) {
            case "map":
                playerSelectedMap.put(playerId, value);
                break;
            case "team":
                playerSelectedTeam.put(playerId, value);
                break;
        }
    }
    
    public String getPlayerSelection(UUID playerId, String type) {
        switch (type.toLowerCase()) {
            case "map":
                return playerSelectedMap.get(playerId);
            case "team":
                return playerSelectedTeam.get(playerId);
        }
        return null;
    }
    
    public void clearPlayerSelection(UUID playerId) {
        playerSelectedMap.remove(playerId);
        playerSelectedTeam.remove(playerId);
    }
    
    public boolean hasCompleteSelection(Player player) {
        UUID playerId = player.getUniqueId();
        return playerSelectedMap.containsKey(playerId) &&
               playerSelectedTeam.containsKey(playerId);
    }
    
    public boolean joinGameWithSelection(Player player) {
        if (!hasCompleteSelection(player)) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        String mapId = playerSelectedMap.get(playerId);
        String team = playerSelectedTeam.get(playerId);
        
        boolean success = plugin.getGameManager().joinGame(player, mapId, team);
        if (success) {
            clearPlayerSelection(playerId);
        }
        
        return success;
    }
} 