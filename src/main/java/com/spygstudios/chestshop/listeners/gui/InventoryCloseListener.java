package com.spygstudios.chestshop.listeners.gui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.DashboardGui;
import com.spygstudios.chestshop.gui.DashboardGui.DashboardHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.spyglib.datacontainer.ItemContainer;

public class InventoryCloseListener implements Listener {

    final ChestShop plugin;

    public InventoryCloseListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder invHolder = inventory.getHolder();

        if (!(invHolder instanceof DashboardHolder || invHolder instanceof ShopHolder || invHolder instanceof PlayersHolder) && inventory.getLocation() != null) {
            Location invLocation = inventory.getLocation();
            Shop shop = Shop.getShop(invLocation);
            if (plugin.getConf().getBoolean("shops.barrier-when-empty")) {
                if (shop == null || !invLocation.getWorld().isChunkLoaded(invLocation.getBlockX() >> 4, invLocation.getBlockZ() >> 4)) {
                    return;
                }
                shop.getHologram().updateHologramRows();
            }
        }

        if (event.getPlayer().getOpenInventory() != null && event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof PlayersHolder holder) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> DashboardGui.open(plugin, (Player) event.getPlayer(), holder.getShop()), 1);
            return;
        }

        if (invHolder instanceof DashboardHolder holder) {
            itemAdding(event, holder);
        }
    }

    private void itemAdding(InventoryCloseEvent event, DashboardHolder holdder) {
        ItemStack item = event.getInventory().getItem(13);
        Shop shop = holdder.getShop();
        if (item == null || ShopUtils.isSimilar(item, shop.getItem())) {
            return;
        }

        if (item.getItemMeta().displayName() != null) {
            return;
        }
        ItemContainer newData = ItemContainer.create(plugin, event.getInventory().getItem(13));
        newData.remove("action");
        shop.setShopItem(item);
        shop.getHologram().updateHologramRows();
    }

    @EventHandler
    public void onShopContainerClosed(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder invHolder = inventory.getHolder();

        Location invLocation = inventory.getLocation();
        if (invHolder.getInventory() == null || invHolder.getInventory().getLocation() == null) {
            return;
        }

        Shop shop = Shop.getShop(invLocation);
        if (shop == null || !invLocation.getWorld().isChunkLoaded(invLocation.getBlockX() >> 4, invLocation.getBlockZ() >> 4)) {
            return;
        }
        shop.getHologram().updateHologramRows();
    }

}
