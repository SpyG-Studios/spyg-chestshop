package com.spygstudios.chestshop.listeners.gui;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.DashboardGui.DashboardHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;

public class InventoryDragListener implements Listener {

    private final List<Class<? extends InventoryHolder>> shopHolders;

    public InventoryDragListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopHolders = List.of(ShopHolder.class, DashboardHolder.class, PlayersHolder.class);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder != null && shopHolders.stream().anyMatch(c -> c.isAssignableFrom(holder.getClass()))) {
            event.setCancelled(true);
        }
    }
}
