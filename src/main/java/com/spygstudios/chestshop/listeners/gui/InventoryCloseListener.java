package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.ChestShopGui;
import com.spygstudios.chestshop.gui.ChestShopGui.ChestShopHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.shop.Shop;

public class InventoryCloseListener implements Listener {

    ChestShop plugin;

    public InventoryCloseListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder invHolder = event.getInventory().getHolder();

        if (plugin.getConf().getBoolean("shops.barrier-when-empty")) {
            Shop shop = Shop.getShop(event.getInventory().getLocation());
            if (shop != null) {
                System.out.println(invHolder);
                shop.getHologram().updateHologramRows();
            }
        }

        if (invHolder instanceof PlayersHolder holder && (event.getPlayer().getOpenInventory() == null || !(event.getPlayer().getOpenInventory().getTopInventory() instanceof PlayersHolder))) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> ChestShopGui.open(plugin, (Player) event.getPlayer(), holder.getShop()), 1);
            return;
        }

        if (invHolder instanceof ChestShopHolder holder) {
            itemAdding(event, holder);
        }

    }

    private void itemAdding(InventoryCloseEvent event, ChestShopHolder holdder) {
        ItemStack item = event.getInventory().getItem(13);
        Shop shop = holdder.getShop();
        if (item == null || item.getType().equals(shop.getMaterial())) {
            return;
        }

        if (item.getItemMeta().displayName() != null) {
            return;
        }
        shop.setMaterial(item.getType());
    }

}
