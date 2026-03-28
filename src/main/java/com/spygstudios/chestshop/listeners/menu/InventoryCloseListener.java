package com.spygstudios.chestshop.listeners.menu;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.shop.Shop;

public class InventoryCloseListener implements Listener {

    public InventoryCloseListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (holder == null || holder.getInventory() == null || holder.getInventory().getLocation() == null) {
            return;
        }

        Location location = inventory.getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return;
        }

        Shop shop = Shop.getShop(location);
        if (shop != null) {
            shop.getHologram().updateHologramRows();
        }
    }
}
