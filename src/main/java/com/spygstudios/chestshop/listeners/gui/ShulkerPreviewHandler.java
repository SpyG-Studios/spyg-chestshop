package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.spygstudios.chestshop.ChestShop;

public class ShulkerPreviewHandler implements Listener {

    public ShulkerPreviewHandler(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ShulkerPreviewHolder)) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            event.setCancelled(true);
            return;
        }

        // Block all direct interactions within the preview container.
        if (clicked.getHolder() instanceof ShulkerPreviewHolder) {
            event.setCancelled(true);
            return;
        }

        // Prevent players from shift-clicking items into the preview.
        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ShulkerPreviewHolder) {
            event.setCancelled(true);
        }
    }

    public static class ShulkerPreviewHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
