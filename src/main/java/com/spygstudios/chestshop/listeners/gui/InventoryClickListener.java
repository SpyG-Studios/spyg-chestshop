package com.spygstudios.chestshop.listeners.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.gui.ChestShopGui;
import com.spygstudios.chestshop.gui.ChestShopGui.ChestShopHolder;
import com.spygstudios.chestshop.gui.PlayersGui;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui;
import com.spygstudios.chestshop.gui.ShopGui.ShopGuiHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopMode;
import com.spygstudios.chestshop.shop.AmountHandler;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.inventory.InventoryUtils;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import net.kyori.adventure.text.Component;

public class InventoryClickListener implements Listener {

    private final ChestShop plugin;
    private final Map<UUID, Long> lastAmountClick;

    public InventoryClickListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.lastAmountClick = new HashMap<>();
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGuiHolder)) {
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
        GuiAction guiAction = GuiAction.valueOf(action);
        ShopGuiHolder holder = (ShopGuiHolder) event.getInventory().getHolder();
        ShopMode currentMode = ShopGui.getPlayerMode(holder.getPlayer());

        switch (guiAction) {
            case SET_ITEM_AMOUNT:
                if (System.currentTimeMillis() - getLastAmountClick(event.getWhoClicked()) < 100) {
                    return;
                }
                ItemStack item = event.getInventory().getItem(13);
                lastAmountClick.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
                
                // Calculate max based on mode: buying from shop vs selling to shop
                int max;
                if (currentMode == ShopMode.CUSTOMER_PURCHASING) {
                    // Player buying from shop - limited by shop inventory
                    int itemsLeft = holder.getShop().getItemsLeft();
                    max = Math.min(item.getMaxStackSize(), itemsLeft);
                } else {
                    // Player selling to shop - limited by player inventory
                    int playerItems = InventoryUtils.countItems(holder.getPlayer().getInventory(), holder.getShop().getMaterial());
                    max = Math.min(item.getMaxStackSize(), playerItems);
                }
                int min = 1;
                int modifier = data.getInt("amount");
                int currentAmount = item.getAmount();
                if (currentAmount + modifier >= max) {
                    currentAmount = max;
                } else if (currentAmount + modifier <= min) {
                    currentAmount = min;
                } else {
                    currentAmount = currentAmount + modifier;
                }
                item.setAmount(currentAmount);
                ItemMeta shopMeta = item.getItemMeta();
                final int finalCurrentAmount = currentAmount;
                String loreKey = currentMode == ShopMode.CUSTOMER_PURCHASING ? "shop.item-to-buy.lore" : "shop.item-to-sell.lore";
                double pricePerItem = currentMode == ShopMode.CUSTOMER_PURCHASING ? holder.getShop().getCustomerPurchasePrice() : holder.getShop().getCustomerSalePrice();
                List<Component> translatedLore = plugin.getGuiConfig().getStringList(loreKey).stream()
                        .map(line -> TranslateColor.translate(line.replace("%price%", String.valueOf(pricePerItem * finalCurrentAmount)))).toList();
                shopMeta.lore(translatedLore);
                item.setItemMeta(shopMeta);
                break;

            case BUY:
                ItemStack shopItem = event.getInventory().getItem(13);
                int amount = shopItem.getAmount();
                holder.getShop().getShopTransactions().sell(holder.getPlayer(), amount);
                if (holder.getShop().getItemsLeft() == 0) {
                    holder.getShop().getHologram().updateHologramRows();
                    holder.getPlayer().closeInventory();
                    Message.SHOP_EMPTY.send(holder.getPlayer());
                }
                break;
            case SELL:
                ItemStack sellItem = event.getInventory().getItem(13);
                int sellAmount = sellItem.getAmount();
                holder.getShop().getShopTransactions().buy(holder.getPlayer(), sellAmount);
                // Update hologram to reflect new shop inventory state
                holder.getShop().getHologram().updateHologramRows();
                break;
            case TOGGLE_MODE:
                ShopMode newMode = currentMode == ShopMode.CUSTOMER_PURCHASING ? ShopMode.CUSTOMER_SELLING : ShopMode.CUSTOMER_PURCHASING;
                // Validate that the new mode is actually supported by the shop
                if ((newMode == ShopMode.CUSTOMER_PURCHASING && !holder.getShop().acceptsCustomerPurchases()) ||
                    (newMode == ShopMode.CUSTOMER_SELLING && !holder.getShop().acceptsCustomerSales())) {
                    // Mode not supported, don't toggle
                    return;
                }
                ShopGui.open(plugin, holder.getPlayer(), holder.getShop(), newMode);
                break;
            case OPEN_SHOP_INVENTORY:
                holder.getShop().openShopInventory(holder.getPlayer());
                break;
            default:
                break;
        }
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
        GuiAction guiAction = GuiAction.valueOf(action);
        switch (guiAction) {
            case REMOVE_PLAYER:
                Shop shop = holder.getShop();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(data.getString("uuid")));
                shop.removePlayer(offlinePlayer.getUniqueId());
                PlayersGui.reloadGui(plugin, event.getInventory());
                break;

            case NEXT:
                holder.setPage(holder.getPage() + 1);
                PlayersGui.reloadGui(plugin, event.getInventory());
                break;
            case BACK:
                holder.setPage(holder.getPage() - 1);
                PlayersGui.reloadGui(plugin, event.getInventory());
                break;
            default:
                break;
        }
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
        ChestShopHolder holder = (ChestShopHolder) event.getInventory().getHolder();
        Shop shop = holder.getShop();
        Player player = holder.getPlayer();
        
        GuiAction action;
        if (event.getClick().isLeftClick()) {
            // Left click to set sell price
            action = GuiAction.SET_SELL_PRICE;
        } else if (event.getClick().isRightClick()) {
            // Right click to set buy price
            action = GuiAction.SET_BUY_PRICE;
        } else {
            return;
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
            // Left click to toggle selling (shop owner selling to customers)
            shop.setCanSell(!shop.acceptsCustomerPurchases());
        } else if (event.getClick().isRightClick()) {
            // Right click to toggle buying (shop owner buying from customers)
            shop.setCanBuy(!shop.acceptsCustomerSales());
        }
        
        // Refresh the GUI to show updated status
        ChestShopGui.open(plugin, player, shop);
    }

    private Long getLastAmountClick(HumanEntity player) {
        if (lastAmountClick.containsKey(player.getUniqueId())) {
            return lastAmountClick.get(player.getUniqueId());
        }
        return 0L;
    }

}
