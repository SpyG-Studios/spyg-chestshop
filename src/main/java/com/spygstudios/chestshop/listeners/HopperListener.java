package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.shop.Shop;

public class HopperListener implements Listener {

    Config config;

    public HopperListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        config = plugin.getConf();
    }

    @EventHandler
    public void onHopper(InventoryMoveItemEvent event) {
        if (!config.getBoolean("shop.hopper-protection")) {
            return;
        }
        if (Shop.isDisabledWorld(event.getSource().getLocation().getWorld())) {
            return;
        }

        if (Shop.getShop(event.getSource().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

}
