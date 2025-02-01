package com.koopalabs.ultrastack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Entity;
import java.util.List;

public class StackListener implements Listener {
    private final UltraStack plugin;
    
    public StackListener(UltraStack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!plugin.getConfig().getBoolean("performance.update-on-spawn")) return;
        if (!plugin.isWorldEnabled(event.getLocation().getWorld())) return;
        
        Item newItem = event.getEntity();
        ItemStack stack = newItem.getItemStack();
        if (stack == null || stack.getType() == Material.AIR) return;

        int maxStack = plugin.getMaxStackSize(stack.getType());
        
        // Get all nearby items first
        List<Item> nearbyItems = new java.util.ArrayList<>();
        for (Entity entity : newItem.getNearbyEntities(2, 2, 2)) {
            if (entity instanceof Item && entity != newItem) {
                Item item = (Item) entity;
                if (item.getItemStack().isSimilar(stack)) {
                    nearbyItems.add(item);
                }
            }
        }

        // If we found similar items, merge them all
        if (!nearbyItems.isEmpty()) {
            int totalAmount = stack.getAmount();
            for (Item item : nearbyItems) {
                totalAmount += item.getItemStack().getAmount();
                item.remove(); // Remove the old items
            }

            // Create a new stack with the total amount
            if (totalAmount > maxStack) {
                // If total exceeds max, create minimum number of full stacks
                while (totalAmount > maxStack) {
                    ItemStack newStack = stack.clone();
                    newStack.setAmount(maxStack);
                    newItem.getWorld().dropItem(newItem.getLocation(), newStack);
                    totalAmount -= maxStack;
                }
                
                // Drop remaining items if any
                if (totalAmount > 0) {
                    stack.setAmount(totalAmount);
                    newItem.setItemStack(stack);
                } else {
                    event.setCancelled(true);
                }
            } else {
                // If total is within max, just set the amount
                stack.setAmount(totalAmount);
                newItem.setItemStack(stack);
            }
        } else {
            // No nearby items, just ensure proper stack size
            if (stack.getAmount() > maxStack) {
                int amount = stack.getAmount();
                stack.setAmount(maxStack);
                
                // Create additional stacks if needed
                amount -= maxStack;
                while (amount > 0) {
                    int nextAmount = Math.min(amount, maxStack);
                    ItemStack nextStack = stack.clone();
                    nextStack.setAmount(nextAmount);
                    newItem.getWorld().dropItem(newItem.getLocation(), nextStack);
                    amount -= nextAmount;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfig().getBoolean("performance.update-on-inventory-click")) return;
        if (!plugin.isWorldEnabled(event.getWhoClicked().getWorld())) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Only handle clicks with items
        if (current == null || current.getType() == Material.AIR) return;
        
        int maxStack = plugin.getMaxStackSize(current.getType());
        if (maxStack <= current.getType().getMaxStackSize()) return;

        // Handle merging items
        if (cursor != null && cursor.getType() != Material.AIR && 
            cursor.isSimilar(current) && 
            event.getAction() == InventoryAction.PLACE_ALL) {
            
            int totalAmount = cursor.getAmount() + current.getAmount();
            if (totalAmount <= maxStack) {
                event.setCancelled(true);
                current.setAmount(totalAmount);
                cursor.setAmount(0);
            } else if (current.getAmount() < maxStack) {
                event.setCancelled(true);
                current.setAmount(maxStack);
                cursor.setAmount(totalAmount - maxStack);
            }
        }

        // Ensure stack size doesn't exceed our max
        if (current.getAmount() > maxStack) {
            current.setAmount(maxStack);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.getConfig().getBoolean("performance.update-on-pickup")) return;
        if (!plugin.isWorldEnabled(event.getEntity().getWorld())) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        ItemStack pickupItem = event.getItem().getItemStack();
        if (pickupItem == null || pickupItem.getType() == Material.AIR) return;

        int maxStack = plugin.getMaxStackSize(pickupItem.getType());
        if (maxStack <= pickupItem.getType().getMaxStackSize()) return;

        // Try to merge with existing stacks first
        PlayerInventory inv = player.getInventory();
        for (ItemStack invItem : inv.getStorageContents()) {
            if (invItem != null && invItem.isSimilar(pickupItem) && invItem.getAmount() < maxStack) {
                int space = maxStack - invItem.getAmount();
                if (pickupItem.getAmount() <= space) {
                    invItem.setAmount(invItem.getAmount() + pickupItem.getAmount());
                    event.getItem().remove();
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // If we can't merge, just ensure the stack size is valid
        pickupItem.setAmount(Math.min(maxStack, pickupItem.getAmount()));
    }
} 