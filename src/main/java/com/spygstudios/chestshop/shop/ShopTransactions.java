package com.spygstudios.chestshop.shop;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.utils.FormatUtils;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class ShopTransactions {

    private final Shop shop;
    private final ChestShop plugin;

    public ShopTransactions(Shop shop) {
        this.shop = shop;
        this.plugin = ChestShop.getInstance();
    }

    public void sell(Player buyer, int amount) {
        if (!shop.acceptsCustomerPurchases()) {
            return;
        }

        int itemsLeft = shop.getItemsLeft();
        int itemCount = itemsLeft < amount ? itemsLeft : amount;

        int batches = itemCount / shop.getQuantity();
        if (batches == 0) {
            Message.SHOP_MINIMUM_PURCHASE.send(buyer, Map.of("%quantity%", String.valueOf(shop.getQuantity())));
            return;
        }
        int effectiveItems = batches * shop.getQuantity();

        if (!InventoryUtils.hasFreeSlot(buyer)) {
            Message.SHOP_INVENTORY_FULL.send(buyer);
            return;
        }

        double itemsPrice = batches * shop.getCustomerPurchasePrice();
        Economy economy = plugin.getEconomy();

        if (!economy.has(buyer, itemsPrice)) {
            Message.NOT_ENOUGH_MONEY.send(buyer, Map.of("%price%", String.valueOf(itemsPrice)));
            return;
        }

        EconomyResponse response = economy.withdrawPlayer(buyer, itemsPrice);

        if (!response.transactionSuccess()) {
            Message.NOT_ENOUGH_MONEY.send(buyer, Map.of("%price%", FormatUtils.formatNumber(itemsPrice)));
            return;
        }

        economy.depositPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);
        Inventory chestInventory = ((Chest) shop.getChestLocation().getBlock().getState()).getInventory();
        int soldItems = ShopUtils.extractItems(chestInventory, buyer.getInventory(), shop.getItem(), effectiveItems);
        itemsLeft = itemsLeft - effectiveItems;

        Message.SHOP_BOUGHT.send(buyer,
                Map.of("%price%", FormatUtils.formatNumber(itemsPrice), "%item%", shop.getItemName(), "%items-left%", String.valueOf(itemsLeft), "%items-bought%", String.valueOf(soldItems)));
        plugin.getDataManager().updateShopSellStats(shop.getOwnerId(), shop.getName(), effectiveItems, itemsPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + shop.getName() + " owned by " + shop.getOwnerId());
                return;
            }
            shop.setSoldItems(shop.getSoldItems() + effectiveItems);
            shop.setMoneyEarned(shop.getMoneyEarned() + itemsPrice);
            shop.setSaved(false);
        });
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_SOLD.send(owner, Map.of(
                    "%price%", FormatUtils.formatNumber(itemsPrice),
                    "%item%", shop.getItemName(),
                    "%player-name%", buyer.getName(),
                    "%items-left%", String.valueOf(itemsLeft),
                    "%items-bought%", String.valueOf(effectiveItems)));
        }
    }

    public void buy(Player seller, int amount) {
        if (!shop.acceptsCustomerSales()) {
            return;
        }

        ItemStack item = shop.getItem();

        int batches = amount / shop.getQuantity();
        if (batches == 0) {
            Message.SHOP_MINIMUM_SALE.send(seller, Map.of("%quantity%", String.valueOf(shop.getQuantity())));
            return;
        }
        int effectiveItems = batches * shop.getQuantity();

        int playerItemCount = ShopUtils.getSellableItemCount(seller.getInventory(), item);
        if (playerItemCount < effectiveItems) {
            Message.NOT_ENOUGH_ITEMS.send(seller, Map.of("%item%", shop.getItemName(), "%amount%", String.valueOf(effectiveItems)));
            seller.closeInventory();
            return;
        }

        Chest chest = (Chest) shop.getChestLocation().getBlock().getState();
        if (!hasChestSpace(chest, item, effectiveItems)) {
            Message.SHOP_CHEST_FULL.send(seller);
            if (shop.isNotify()) {
                Player owner = Bukkit.getPlayer(shop.getOwnerId());
                if (owner != null) {
                    Message.SHOP_CHEST_FULL_OWNER.send(owner, Map.of(
                            "%player-name%", seller.getName(),
                            "%item%", shop.getItemName(),
                            "%amount%", String.valueOf(effectiveItems),
                            "%shop-name%", shop.getName()));
                }
            }
            return;
        }

        double itemsPrice = batches * shop.getCustomerSalePrice();
        Economy economy = plugin.getEconomy();

        if (!economy.has(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice)) {
            Message.SHOP_OWNER_NO_MONEY.send(seller);
            if (shop.isNotify()) {
                Player owner = Bukkit.getPlayer(shop.getOwnerId());
                if (owner != null) {
                    Message.SHOP_OWNER_NO_MONEY_OWNER.send(owner, Map.of("%player-name%", seller.getName(), "%item%", shop.getName(), "%price%", FormatUtils.formatNumber(itemsPrice)));
                }
            }
            return;
        }
        EconomyResponse response = economy.withdrawPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);
        if (!response.transactionSuccess()) {
            plugin.getLogger().warning("Failed to withdraw money from shop owner " + shop.getOwnerId() + " for shop " + shop.getName() + ": " + response.errorMessage);
            return;
        }

        Inventory chestInventory = chest.getInventory();
        int soldItems = ShopUtils.extractItems(seller.getInventory(), chestInventory, item, effectiveItems);

        economy.depositPlayer(seller, itemsPrice);

        plugin.getDataManager().updateShopBuyStats(shop.getOwnerId(), shop.getName(), effectiveItems, itemsPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + shop.getName() + " owned by " + shop.getOwnerId());
                return;
            }
            shop.setBoughtItems(shop.getBoughtItems() + effectiveItems);
            shop.setMoneySpent(shop.getMoneySpent() + itemsPrice);
            shop.setSaved(false);
        });

        Message.SHOP_SOLD_TO.send(seller, Map.of(
                "%price%", FormatUtils.formatNumber(itemsPrice),
                "%item%", shop.getItemName(),
                "%items-sold%", String.valueOf(soldItems)));

        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_BOUGHT_FROM.send(owner,
                    Map.of(
                            "%price%", FormatUtils.formatNumber(itemsPrice),
                            "%item%", shop.getItemName(),
                            "%player-name%", seller.getName(),
                            "%items-bought%", String.valueOf(soldItems)));
        }
    }

    private boolean hasChestSpace(Chest chest, ItemStack item, int amount) {
        int maxStackSize = item.getMaxStackSize();
        int remainingAmount = amount;

        for (ItemStack i : chest.getInventory().getContents()) {
            if (remainingAmount <= 0)
                break;

            if (i == null) {
                remainingAmount -= maxStackSize;
            } else if (ShopUtils.isSimilar(i, item) && i.getAmount() < maxStackSize) {
                remainingAmount -= (maxStackSize - i.getAmount());
            }
        }

        return remainingAmount <= 0;
    }

}
