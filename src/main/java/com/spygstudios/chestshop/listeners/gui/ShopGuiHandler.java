package com.spygstudios.chestshop.listeners.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.enums.ShopMode;
import com.spygstudios.chestshop.gui.ShopGui;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import net.kyori.adventure.text.Component;

public class ShopGuiHandler implements Listener {

    private final ChestShop plugin;
    private final Map<UUID, Long> lastAmountClick;

    public ShopGuiHandler(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.lastAmountClick = new HashMap<>();
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopHolder)) {
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
        ShopHolder holder = (ShopHolder) event.getInventory().getHolder();
        Player player = holder.getPlayer();
        ShopMode currentMode = ShopGui.getPlayerMode(player);
        switch (guiAction) {
            case SET_ITEM_AMOUNT:
                if (System.currentTimeMillis() - getLastClick(event.getWhoClicked()) < 100) {
                    return;
                }
                ItemStack item = event.getInventory().getItem(13);
                lastAmountClick.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());

                // Calculate max based on mode: buying from shop vs selling to shop
                int max;
                if (currentMode == ShopMode.CUSTOMER_PURCHASING) {
                    int itemsLeft = holder.getShop().getItemsLeft();
                    max = Math.min(item.getMaxStackSize(), itemsLeft);
                } else {
                    int playerItems = ShopUtils.countDurableItemsInInventory(player.getInventory(), holder.getShop().getMaterial());
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
                holder.getShop().getShopTransactions().sell(player, amount);
                if (holder.getShop().getItemsLeft() == 0) {
                    holder.getShop().getHologram().updateHologramRows();
                    player.closeInventory();
                    Message.SHOP_EMPTY.send(player);
                }
                break;
            case SELL:
                ItemStack sellItem = event.getInventory().getItem(13);
                int sellAmount = sellItem.getAmount();
                holder.getShop().getShopTransactions().buy(player, sellAmount);
                break;
            case TOGGLE_MODE:
                ShopMode newMode = currentMode == ShopMode.CUSTOMER_PURCHASING ? ShopMode.CUSTOMER_SELLING : ShopMode.CUSTOMER_PURCHASING;
                if ((newMode == ShopMode.CUSTOMER_PURCHASING && !holder.getShop().acceptsCustomerPurchases()) ||
                        (newMode == ShopMode.CUSTOMER_SELLING && !holder.getShop().acceptsCustomerSales())) {
                    return;
                }
                ShopGui.setPlayerMode(player, newMode);
                ShopGui.open(plugin, player, holder.getShop(), newMode);
                break;
            case OPEN_SHOP_INVENTORY:
                holder.getShop().openShopInventory(player);
                break;
            default:
                break;
        }
    }

    private Long getLastClick(HumanEntity player) {
        return lastAmountClick.getOrDefault(player.getUniqueId(), 0L);
    }

}
