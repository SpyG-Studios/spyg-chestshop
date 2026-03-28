package com.spygstudios.chestshop.listeners.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.menu.holder.BaseHolder;

public class InventoryDragListener implements Listener {

    public InventoryDragListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BaseHolder) {
            event.setCancelled(true);
        }
    }
}
