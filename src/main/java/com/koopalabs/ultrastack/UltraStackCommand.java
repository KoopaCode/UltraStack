package com.koopalabs.ultrastack;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class UltraStackCommand implements CommandExecutor {
    private final UltraStack plugin;
    
    public UltraStackCommand(UltraStack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultrastack.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix") + " " +
                plugin.getConfig().getString("messages.reload")));
            return true;
        }
        
        return false;
    }
} 