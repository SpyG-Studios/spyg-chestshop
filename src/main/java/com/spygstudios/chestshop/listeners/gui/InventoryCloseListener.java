package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.MainGui.ShopHolder;
import com.spygstudios.spyglib.inventory.InventoryUtils;

public class InventoryCloseListener implements Listener {

    public InventoryCloseListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getInventory().getHolder() instanceof ShopHolder) {
            itemAdding(event);
            return;
        }

    }

    private void itemAdding(InventoryCloseEvent event) {

        ItemStack[] contents = event.getInventory().getContents();

        Material material = Material.getMaterial(((ShopHolder) event.getInventory().getHolder()).getMaterial());

        boolean hasTheMaterial = true;

        for (ItemStack item : contents) {

            if (item == null) {
                continue;
            }

            if (item.getType() == material) {
                hasTheMaterial = false;
                break;
            }

            if (item.getType() != material) {
                hasTheMaterial = true;
            }

        }

        Player player = ((ShopHolder) event.getInventory().getHolder()).getPlayer();
        int amountRequested = ((ShopHolder) event.getInventory().getHolder()).getAmount();
        int givenAmount = InventoryUtils.countItems(event.getInventory(), material);

        for (ItemStack item : contents) {
            if (item == null) {
                continue;
            }

            if (item.getType() != material) {
                player.getInventory().addItem(item);
            }
        }
    }

}
