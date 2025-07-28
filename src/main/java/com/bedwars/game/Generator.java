package com.bedwars.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Generator {
    
    private final Location location;
    private final Material material;
    private final int intervalSeconds;
    private final int maxItems;
    
    private BukkitTask task;
    private final List<Item> droppedItems;
    
    public Generator(Location location, Material material, int intervalSeconds, int maxItems) {
        this.location = location;
        this.material = material;
        this.intervalSeconds = intervalSeconds;
        this.maxItems = maxItems;
        this.droppedItems = new ArrayList<>();
    }
    
    public void start() {
        if (task != null) {
            task.cancel();
        }
        
        task = new BukkitRunnable() {
            @Override
            public void run() {
                generateItem();
            }
        }.runTaskTimer(org.bukkit.Bukkit.getPluginManager().getPlugin("Bedwars"), 
                      intervalSeconds * 20L, intervalSeconds * 20L);
    }
    
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
    
    private void generateItem() {
        World world = location.getWorld();
        if (world == null) return;
        
        // Remove old items if we have too many
        droppedItems.removeIf(item -> item.isDead() || !item.isValid());
        
        if (droppedItems.size() >= maxItems) {
            return;
        }
        
        // Create item entity
        ItemStack itemStack = new ItemStack(material, 1);
        Item item = world.dropItem(location, itemStack);
        
        // Set item properties
        item.setVelocity(new Vector(0, 0.1, 0));
        item.setPickupDelay(0);
        
        droppedItems.add(item);
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public int getIntervalSeconds() {
        return intervalSeconds;
    }
    
    public int getMaxItems() {
        return maxItems;
    }
    
    public List<Item> getDroppedItems() {
        return new ArrayList<>(droppedItems);
    }
    
    public void clearItems() {
        for (Item item : droppedItems) {
            if (!item.isDead()) {
                item.remove();
            }
        }
        droppedItems.clear();
    }
} 