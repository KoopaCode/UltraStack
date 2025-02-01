package com.koopalabs.ultrastack;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class UltraStack extends JavaPlugin implements Listener {
    private static UltraStack instance;
    private FileConfiguration config;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadConfig();
        
        // Register events
        getServer().getPluginManager().registerEvents(new StackListener(this), this);
        
        // Register commands
        getCommand("ultrastack").setExecutor(new UltraStackCommand(this));
    }
    
    public void loadConfig() {
        reloadConfig();
        config = getConfig();
    }
    
    public boolean isWorldEnabled(World world) {
        if (!config.getBoolean("enabled")) return false;
        
        if (config.getStringList("worlds.disabled-worlds").contains(world.getName())) {
            return false;
        }
        
        java.util.List<String> enabledWorlds = config.getStringList("worlds.enabled-worlds");
        return enabledWorlds.contains("all") || enabledWorlds.contains(world.getName());
    }
    
    public int getMaxStackSize(Material material) {
        String mode = config.getString("stack-settings.mode", "all");
        
        if (mode.equalsIgnoreCase("all")) {
            return config.getInt("stack-settings.default-stack-size", 64000);
        }
        
        String path = "stack-settings.items." + material.name();
        if (config.contains(path) && config.getBoolean(path + ".enabled")) {
            return config.getInt(path + ".max-stack", 64);
        }
        
        return material.getMaxStackSize();
    }
    
    public static UltraStack getInstance() {
        return instance;
    }
} 