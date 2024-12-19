package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

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
        ItemStack item = event.getInventory().getItem(13);
        Shop shop = holdder.getShop();
        if (item == null || item.getType().equals(shop.getMaterial())) {
            return;
        }
        shop.setMaterial(item.getType());
    }

}
