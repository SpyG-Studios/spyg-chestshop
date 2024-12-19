package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.shop.Shop;

public class InventoryCloseListener implements Listener {

    public InventoryCloseListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getInventory().getHolder() instanceof ShopHolder holder) {
            itemAdding(event, holder);
            return;
        }

    }

    private void itemAdding(InventoryCloseEvent event, ShopHolder holdder) {
        Material material = event.getInventory().getItem(13).getType();
        Shop shop = holdder.getShop();
        if (material == Material.AIR || material.equals(shop.getMaterial())) {
            return;
        }
        shop.setMaterial(material);
    }

}
