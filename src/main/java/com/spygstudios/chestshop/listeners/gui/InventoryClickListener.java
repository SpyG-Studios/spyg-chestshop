package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.spyglib.persistentdata.PersistentData;

public class InventoryClickListener implements Listener {

    private ChestShop plugin;

    public InventoryClickListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        if (event.getInventory().getHolder() instanceof ShopHolder) {
            shopGui(event);
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getCursor() == null) {
            return;
        }
        System.out.println(event.getCursor().getType());
    }

    private void shopGui(InventoryClickEvent event) {
        PersistentData data = new PersistentData(plugin, event.getCurrentItem());
        String action = data.getString("action");
        if (action == null) {
            return;
        }

        Player player = ((ShopHolder) event.getInventory().getHolder()).getPlayer();

        switch (action) {
        case "change-material":
            if (event.getCursor() == null || event.getCursor().getType().isAir() || event.getCursor().getType().equals(event.getCurrentItem().getType())) {
                return;
            }
            event.getInventory().setItem(13, new ItemStack(event.getCursor()));
            PersistentData newData = new PersistentData(plugin, event.getInventory().getItem(13));
            newData.set("action", "change-material");
            newData.save();
            break;
        case "close":
            player.closeInventory();
            break;
        }
    }

}
