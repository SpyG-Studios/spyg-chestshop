package com.spygstudios.chestshop.shop;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
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
        if (!InventoryUtils.hasFreeSlot(buyer)) {
            Message.SHOP_INVENTORY_FULL.send(buyer);
            return;
        }

        double itemsPrice = itemCount * shop.getCustomerPurchasePrice();
        Economy economy = plugin.getEconomy();
        EconomyResponse response = economy.withdrawPlayer(buyer, itemsPrice);

        if (!response.transactionSuccess()) {
            Message.NOT_ENOUGH_MONEY.send(buyer, Map.of("%price%", String.valueOf(itemsPrice)));
            return;
        }

        economy.depositPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);
        Inventory chestInventory = ((Chest) shop.getChestLocation().getBlock().getState()).getInventory();
        int soldItems = ShopUtils.extractItems(chestInventory, buyer.getInventory(), shop.getItem(), itemCount);
        itemsLeft = itemsLeft - itemCount;

        Message.SHOP_BOUGHT.send(buyer,
                Map.of("%price%", String.valueOf(itemsPrice), "%item%", shop.getItemName(), "%items-left%", String.valueOf(itemsLeft), "%items-bought%", String.valueOf(soldItems)));
        plugin.getDataManager().updateShopSellStats(shop.getOwnerId(), shop.getName(), itemCount, itemsPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + shop.getName() + " owned by " + shop.getOwnerId());
                return;
            }
            shop.setSoldItems(shop.getSoldItems() + itemCount);
            shop.setMoneyEarned(shop.getMoneyEarned() + itemsPrice);
            shop.setSaved(false);
        });
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_SOLD.send(owner, Map.of(
                    "%price%", String.valueOf(itemsPrice),
                    "%item%", shop.getItemName(),
                    "%player-name%", buyer.getName(),
                    "%items-left%", String.valueOf(itemsLeft),
                    "%items-bought%", String.valueOf(itemCount)));
        }
    }

    public void buy(Player seller, int amount) {
        if (!shop.acceptsCustomerSales()) {
            return;
        }

        ItemStack item = shop.getItem();
        int playerItemCount = ShopUtils.getSellableItemCount(seller.getInventory(), item);
        if (playerItemCount < amount) {
            Message.NOT_ENOUGH_ITEMS.send(seller, Map.of("%item%", shop.getItemName(), "%amount%", String.valueOf(amount)));
            seller.closeInventory();
            return;
        }

        Chest chest = (Chest) shop.getChestLocation().getBlock().getState();
        if (!hasChestSpace(chest, item, amount)) {
            Message.SHOP_CHEST_FULL.send(seller);
            if (!shop.isNotify()) {
                return;
            }
            Player owner = Bukkit.getPlayer(shop.getOwnerId());
            if (owner != null) {
                Message.SHOP_CHEST_FULL_OWNER.send(owner, Map.of(
                        "%player-name%", seller.getName(),
                        "%item%", shop.getName(),
                        "%amount%", String.valueOf(amount),
                        "%shop-name%", shop.getName()));
            }
        }

        double itemsPrice = amount * shop.getCustomerSalePrice();
        Economy economy = plugin.getEconomy();
        EconomyResponse response = economy.withdrawPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);

        if (!response.transactionSuccess()) {
            Message.SHOP_OWNER_NO_MONEY.send(seller);
            if (!shop.isNotify()) {
                return;
            }
            Player owner = Bukkit.getPlayer(shop.getOwnerId());
            if (owner != null) {
                Message.SHOP_OWNER_NO_MONEY_OWNER.send(owner, Map.of("%player-name%", seller.getName(), "%item%", shop.getName(), "%price%", String.valueOf(itemsPrice)));
            }
        }
        Inventory chestInventory = chest.getInventory();
        int soldItems = ShopUtils.extractItems(seller.getInventory(), chestInventory, item, amount);

        economy.depositPlayer(seller, itemsPrice);

        plugin.getDataManager().updateShopBuyStats(shop.getOwnerId(), shop.getName(), amount, itemsPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + shop.getName() + " owned by " + shop.getOwnerId());
                return;
            }
            shop.setBoughtItems(shop.getBoughtItems() + amount);
            shop.setMoneySpent(shop.getMoneySpent() + itemsPrice);
            shop.setSaved(false);
        });

        Message.SHOP_SOLD_TO.send(seller, Map.of(
                "%price%", String.valueOf(itemsPrice),
                "%item%", shop.getItemName(),
                "%items-sold%", String.valueOf(soldItems)));

        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_BOUGHT_FROM.send(owner,
                    Map.of(
                            "%price%", String.valueOf(itemsPrice),
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
