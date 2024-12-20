package com.spygstudios.chestshop.listeners.gui;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.persistentdata.PersistentData;

public class InventoryClickListener implements Listener {

    private ChestShop plugin;

    public InventoryClickListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopHolder)) {
            return;
        }
        if (event.getClickedInventory() == null || event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (event.getSlot() == 13) {
            changeShopMaterial(event);
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        shopGui(event);

    }

    private void shopGui(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        PersistentData data = new PersistentData(plugin, clickedItem);
        String action = data.getString("action");
        if (action == null) {
            return;
        }

        Player player = ((ShopHolder) event.getInventory().getHolder()).getPlayer();

        switch (action) {
        case "change-material":
            changeShopMaterial(event);
            break;
        case "toggle-notify":
            Shop shop = Shop.getShop(data.getString("shop"));
            shop.setNotify(!shop.isNotify());
            ItemStack notifyItem = clickedItem;
            ItemMeta notifyMeta = notifyItem.getItemMeta();
            notifyMeta.lore(Arrays.asList(TranslateColor.translate(shop.isNotify() ? plugin.getGuiConfig().getString("shop.notify.on") : plugin.getGuiConfig().getString("shop.notify.off"))));
            notifyItem.setItemMeta(notifyMeta);
            player.updateInventory();
            break;
        case "close":
            player.closeInventory();
            break;
        }
    }

    private void changeShopMaterial(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType().isAir()) {
            return;
        }
        event.getInventory().setItem(13, new ItemStack(event.getCursor().getType()));
        PersistentData newData = new PersistentData(plugin, event.getInventory().getItem(13));
        newData.set("action", "change-material");
        newData.save();
    }

}
