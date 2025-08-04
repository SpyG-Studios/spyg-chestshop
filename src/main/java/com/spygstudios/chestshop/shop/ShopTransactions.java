package com.spygstudios.chestshop.shop;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private final ShopFile shopFile;

    public ShopTransactions(Shop shop, ShopFile shopFile) {
        this.shopFile = shopFile;
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
        int soldItems = ShopUtils.extractItems(chestInventory, buyer.getInventory(), shop.getMaterial(), itemCount);
        itemsLeft = itemsLeft - itemCount;

        Message.SHOP_BOUGHT.send(buyer,
                Map.of("%price%", String.valueOf(itemsPrice), "%material%", shop.getMaterial().name(), "%items-left%", String.valueOf(itemsLeft), "%items-bought%", String.valueOf(soldItems)));
        shopFile.overwriteSet("shops." + shop.getName() + ".sold-items", shopFile.getInt("shops." + shop.getName() + ".sold-items") + soldItems);
        shopFile.overwriteSet("shops." + shop.getName() + ".money-earned", shopFile.getDouble("shops." + shop.getName() + ".money-earned") + itemsPrice);
        shopFile.save();
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_SOLD.send(owner, Map.of("%price%", String.valueOf(itemsPrice), "%material%", shop.getMaterial().name(), "%player-name%", buyer.getName(), "%items-left%",
                    String.valueOf(itemsLeft), "%items-bought%", String.valueOf(itemCount)));
        }
    }

    public void buy(Player seller, int amount) {
        if (!shop.acceptsCustomerSales()) {
            return;
        }

        Material material = shop.getMaterial();
        int playerItemCount = ShopUtils.countDurableItemsInInventory(seller.getInventory(), material);
        if (playerItemCount < amount) {
            Message.NOT_ENOUGH_ITEMS.send(seller, Map.of("%material%", material.name(), "%amount%", String.valueOf(amount)));
            seller.closeInventory();
            return;
        }

        Chest chest = (Chest) shop.getChestLocation().getBlock().getState();
        if (!hasChestSpace(chest, material, amount)) {
            Message.SHOP_CHEST_FULL.send(seller);
            if (!shop.isNotify()) {
                return;
            }
            Player owner = Bukkit.getPlayer(shop.getOwnerId());
            if (owner != null) {
                Message.SHOP_CHEST_FULL_OWNER.send(owner, Map.of(
                        "%player-name%", seller.getName(),
                        "%material%", material.name(),
                        "%amount%", String.valueOf(amount),
                        "%shop-name%", shop.getName()));
            }
        }

        double itemsPrice = amount * shop.getCustomerSalePrice();
        Economy economy = plugin.getEconomy();

        if (economy.getBalance(Bukkit.getOfflinePlayer(shop.getOwnerId())) < itemsPrice) {
            Message.SHOP_OWNER_NO_MONEY.send(seller);
            if (!shop.isNotify()) {
                return;
            }
            Player owner = Bukkit.getPlayer(shop.getOwnerId());
            if (owner != null) {
                Message.SHOP_OWNER_NO_MONEY_OWNER.send(owner, Map.of("%player-name%", seller.getName(), "%material%", material.name(), "%price%", String.valueOf(itemsPrice)));
            }
        }
        Inventory chestInventory = ((Chest) shop.getChestLocation().getBlock().getState()).getInventory();
        int soldItems = ShopUtils.extractItems(seller.getInventory(), chestInventory, material, amount);

        EconomyResponse withdrawResponse = economy.withdrawPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);
        if (withdrawResponse.transactionSuccess()) {
            economy.depositPlayer(seller, itemsPrice);
        }

        shopFile.overwriteSet("shops." + shop.getName() + ".bought-items", shopFile.getInt("shops." + shop.getName() + ".bought-items") + soldItems);
        shopFile.save();

        shopFile.overwriteSet("shops." + shop.getName() + ".money-earned", shopFile.getDouble("shops." + shop.getName() + ".money-spent") + itemsPrice);
        Message.SHOP_SOLD_TO.send(seller, Map.of("%price%", String.valueOf(itemsPrice), "%material%", material.name(), "%items-sold%", String.valueOf(soldItems)));

        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_BOUGHT_FROM.send(owner,
                    Map.of("%price%", String.valueOf(itemsPrice), "%material%", material.name(), "%player-name%", seller.getName(), "%items-bought%", String.valueOf(soldItems)));
        }
    }

    private boolean hasChestSpace(Chest chest, Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        int remainingAmount = amount;

        for (ItemStack item : chest.getInventory().getContents()) {
            if (remainingAmount <= 0)
                break;

            if (item == null) {
                remainingAmount -= maxStackSize;
            } else if (item.getType() == material && item.getAmount() < maxStackSize) {
                remainingAmount -= (maxStackSize - item.getAmount());
            }
        }

        return remainingAmount <= 0;
    }

}
