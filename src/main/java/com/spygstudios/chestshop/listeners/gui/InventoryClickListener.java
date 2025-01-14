package com.spygstudios.chestshop.listeners.gui;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.gui.ShopGui;
import com.spygstudios.chestshop.gui.ChestShopGui;
import com.spygstudios.chestshop.gui.ChestShopGui.ChestShopHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.shop.AmountHandler;
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
    public void onShopGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ChestShopHolder)) {
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

    @EventHandler
    public void onPlayersGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlayersHolder)) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        event.setCancelled(true);
        PersistentData data = new PersistentData(plugin, clickedItem);
        String action = data.getString("action");
        if (action == null) {
            return;
        }
        PlayersHolder holder = (PlayersHolder) event.getInventory().getHolder();
        Player player = holder.getPlayer();
        Shop shop = holder.getShop();
        GuiAction guiAction = GuiAction.valueOf(action);
        switch (guiAction) {
        case REMOVE_PLAYER:
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(data.getString("uuid")));
            shop.removePlayer(offlinePlayer.getUniqueId());
            ShopGui.open(plugin, player, shop);
            break;
        case BACK:
            ChestShopGui.open(plugin, player, shop);
            break;
        default:
            break;
        }
    }

    private void shopGui(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        PersistentData data = new PersistentData(plugin, clickedItem);
        String action = data.getString("action");
        if (action == null) {
            return;
        }

        Player player = ((ChestShopHolder) event.getInventory().getHolder()).getPlayer();
        Shop shop = Shop.getShop(data.getString("shop"));
        GuiAction guiAction = GuiAction.valueOf(action);
        switch (guiAction) {
        case SET_MATERIAL:
            changeShopMaterial(event);
            break;
        case TOGGLE_NOTIFY:
            shop.setNotify(!shop.isNotify());
            ItemStack notifyItem = clickedItem;
            ItemMeta notifyMeta = notifyItem.getItemMeta();
            notifyMeta.lore(Arrays.asList(TranslateColor.translate(shop.isNotify() ? plugin.getGuiConfig().getString("shop.notify.on") : plugin.getGuiConfig().getString("shop.notify.off"))));
            notifyItem.setItemMeta(notifyMeta);
            player.updateInventory();
            break;
        case SET_ITEM_AMOUNT, SET_ITEM_PRICE:
            if (AmountHandler.getPendingAmount(player) != null) {
                AmountHandler.getPendingAmount(player).cancel();
            }
            new AmountHandler(player, shop, guiAction);
            event.getInventory().close();
            break;
        case OPEN_PLAYERS:
            ShopGui.open(plugin, player, shop);
            break;
        case OPEN_SHOP_INVENTORY:
            shop.openShopInventory(player);
            break;
        case CLOSE:
            player.closeInventory();
            break;
        default:
            break;
        }
    }

    private void changeShopMaterial(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType().isAir()) {
            return;
        }
        event.getInventory().setItem(13, new ItemStack(event.getCursor().getType()));
        PersistentData newData = new PersistentData(plugin, event.getInventory().getItem(13));
        newData.set("action", GuiAction.SET_MATERIAL.name());
        newData.save();
    }

}
