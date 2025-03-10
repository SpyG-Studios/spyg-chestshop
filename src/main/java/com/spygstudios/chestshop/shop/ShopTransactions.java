package com.spygstudios.chestshop.shop;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
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
        int itemsLeft = shop.getItemsLeft();
        int itemCount = itemsLeft < amount ? itemsLeft : amount;
        if (!InventoryUtils.hasFreeSlot(buyer)) {
            Message.SHOP_INVENTORY_FULL.send(buyer);
            return;
        }

        int itemsPrice = itemCount * shop.getPrice();
        Economy economy = plugin.getEconomy();
        EconomyResponse response = economy.withdrawPlayer(buyer, itemsPrice);

        if (!response.transactionSuccess()) {
            Message.NOT_ENOUGH_MONEY.send(buyer, Map.of("%price%", String.valueOf(itemsPrice)));
            return;
        }

        economy.depositPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);
        extractItems(buyer, (Chest) shop.getChestLocation().getBlock().getState(), itemCount);
        itemsLeft = itemsLeft - itemCount;

        Message.SHOP_BOUGHT.send(buyer,
                Map.of("%price%", String.valueOf(itemsPrice), "%material%", shop.getMaterial().name(), "%items-left%", String.valueOf(itemsLeft), "%items-bought%", String.valueOf(itemCount)));
        shopFile.overwriteSet("shops." + shop.getName() + ".sold-items", shopFile.getInt("shops." + shop.getName() + ".sold-items") + itemCount);
        shopFile.overwriteSet("shops." + shop.getName() + ".money-earned", shopFile.getDouble("shops." + shop.getName() + ".money-earned") + itemsPrice);
        shopFile.save();
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_SOLD.send(owner, Map.of("%price%", String.valueOf(itemsPrice), "%material%", shop.getMaterial().name(), "%player-name%", buyer.getName(), "%items-left%",
                    String.valueOf(itemsLeft), "%items-bought%", String.valueOf(itemCount)));
        }
    }

    private int extractItems(Player buyer, Chest chest, int itemCount) {
        Material material = shop.getMaterial();
        while (itemCount > 0) {
            int amountToTransfer = Math.min(itemCount, material.getMaxStackSize());
            ItemStack stackToAdd = new ItemStack(material, amountToTransfer);
            buyer.getInventory().addItem(stackToAdd);
            for (ItemStack chestItem : chest.getInventory().getContents()) {
                if (chestItem != null && chestItem.getType() == material) {
                    int chestItemAmount = chestItem.getAmount();
                    int removeAmount = Math.min(amountToTransfer, chestItemAmount);
                    chestItem.setAmount(chestItemAmount - removeAmount); // Csökkentjük a ládában lévő mennyiséget
                    amountToTransfer -= removeAmount; // Csökkentjük az áthelyezendő mennyiséget
                    if (amountToTransfer <= 0) {
                        break;
                    }
                }
            }
            itemCount -= Math.min(itemCount, material.getMaxStackSize());
        }
        return itemCount;
    }

}
