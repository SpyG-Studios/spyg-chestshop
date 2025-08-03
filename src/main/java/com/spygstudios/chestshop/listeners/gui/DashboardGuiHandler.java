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
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.gui.ChestShopGui;
import com.spygstudios.chestshop.gui.ChestShopGui.ChestShopHolder;
import com.spygstudios.chestshop.gui.PlayersGui;
import com.spygstudios.chestshop.shop.AmountHandler;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.persistentdata.PersistentData;

public class DashboardGuiHandler implements Listener {

    private final ChestShop plugin;

    public DashboardGuiHandler(ChestShop plugin) {
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

        if (event.getSlot() == 11) {
            handlePriceSetting(event);
            return;
        }

        if (event.getSlot() == 15) {
            handleBuySellToggle(event);
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        chestShopGui(event);
    }

    private void chestShopGui(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        PersistentData data = new PersistentData(plugin, clickedItem);
        String action = data.getString("action");
        if (action == null) {
            return;
        }
        ChestShopHolder holder = (ChestShopHolder) event.getInventory().getHolder();
        Player player = holder.getPlayer();
        Shop shop = holder.getShop();
        GuiAction guiAction = GuiAction.valueOf(action);
        switch (guiAction) {
            case SET_MATERIAL:
                changeShopMaterial(event);
                break;
            case TOGGLE_NOTIFY:
                shop.setNotify(!shop.isNotify());
                ItemStack notifyItem = clickedItem;
                ItemMeta notifyMeta = notifyItem.getItemMeta();
                notifyMeta
                        .lore(Arrays.asList(TranslateColor.translate(shop.isNotify()
                                ? plugin.getGuiConfig().getString("chestshop.notify.on")
                                : plugin.getGuiConfig().getString("chestshop.notify.off"))));
                notifyItem.setItemMeta(notifyMeta);
                player.updateInventory();
                break;
            case SET_ITEM_PRICE:
            case SET_SELL_PRICE:
            case SET_BUY_PRICE:
                if (AmountHandler.getPendingAmount(player) != null) {
                    AmountHandler.getPendingAmount(player).cancel();
                }
                new AmountHandler(player, shop, guiAction);
                event.getInventory().close();
                break;
            case OPEN_PLAYERS:
                PlayersGui.open(plugin, player, shop);
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

    private void handlePriceSetting(InventoryClickEvent event) {
        if (!event.getClick().isLeftClick() && !event.getClick().isRightClick()) {
            return;
        }
        ChestShopHolder holder = (ChestShopHolder) event.getInventory().getHolder();
        Shop shop = holder.getShop();
        Player player = holder.getPlayer();

        GuiAction action = GuiAction.SET_SELL_PRICE;
        if (event.getClick().isRightClick()) {
            action = GuiAction.SET_BUY_PRICE;
        }

        if (AmountHandler.getPendingAmount(player) != null) {
            AmountHandler.getPendingAmount(player).cancel();
        }
        new AmountHandler(player, shop, action);
        event.getInventory().close();
    }

    private void handleBuySellToggle(InventoryClickEvent event) {
        ChestShopHolder holder = (ChestShopHolder) event.getInventory().getHolder();
        Shop shop = holder.getShop();
        Player player = holder.getPlayer();

        if (event.getClick().isLeftClick()) {
            shop.setCanSell(!shop.acceptsCustomerPurchases());
        } else if (event.getClick().isRightClick()) {
            shop.setCanBuy(!shop.acceptsCustomerSales());
        }

        ChestShopGui.open(plugin, player, shop);
    }

}
