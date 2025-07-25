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
import com.spygstudios.chestshop.gui.ChestShopGui.ChestShopHolder;
import com.spygstudios.chestshop.gui.PlayersGui;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopGuiHolder;
import com.spygstudios.chestshop.shop.AmountHandler;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
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
        switch (guiAction) {
            case SET_ITEM_AMOUNT:
                if (System.currentTimeMillis() - getLastAmountClick(event.getWhoClicked()) < 100) {
                    return;
                }
                ItemStack item = event.getInventory().getItem(13);
                lastAmountClick.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
                int itemsLeft = holder.getShop().getItemsLeft();
                int max = item.getMaxStackSize() > itemsLeft ? itemsLeft : item.getMaxStackSize();
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
                List<Component> translatedLore = plugin.getGuiConfig().getStringList("shop.item-to-buy.lore").stream()
                        .map(line -> TranslateColor.translate(line.replace("%price%", String.valueOf(holder.getShop().getPrice() * finalCurrentAmount)))).toList();
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

    private Long getLastAmountClick(HumanEntity player) {
        if (lastAmountClick.containsKey(player.getUniqueId())) {
            return lastAmountClick.get(player.getUniqueId());
        }
        return 0L;
    }

}
