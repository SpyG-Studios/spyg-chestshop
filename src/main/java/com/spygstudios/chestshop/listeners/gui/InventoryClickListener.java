package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.MainGui;
import com.spygstudios.chestshop.gui.MainGui.ShopHolder;
import com.spygstudios.spyglib.persistentdata.PersistentData;

public class InventoryClickListener implements Listener {

    private ChestShop plugin;

    public InventoryClickListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null) {
            return;
        }

        if (event.getInventory().getHolder() instanceof MainGui) {
            mainGui(event);
            return;
        }
    }

    private void requestingGui(InventoryClickEvent event) {

        int slot = event.getSlot();

        // if (slot != 13 && event.getClickedInventory().getHolder() instanceof
        // ItemRequestingHolder) {
        // event.setCancelled(true);
        // }

    }

    private void mainGui(InventoryClickEvent event) {
        PersistentData data = new PersistentData(plugin, event.getCurrentItem());

        String action = data.getString("action");

        if (action == null) {
            return;
        }

        event.setCancelled(true);

        Player player = ((ShopHolder) event.getInventory().getHolder()).getPlayer();

        switch (action) {
        case "close":
            player.closeInventory();
            break;
        }
    }

}
