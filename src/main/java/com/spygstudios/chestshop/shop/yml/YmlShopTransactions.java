package com.spygstudios.chestshop.shop.yml;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.interfaces.ShopFile;
import com.spygstudios.chestshop.interfaces.ShopTransactions;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class YmlShopTransactions implements ShopTransactions {

    private final Shop shop;
    private final ChestShop plugin;
    private final ShopFile shopFile;

    public YmlShopTransactions(Shop shop, ShopFile shopFile) {
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

        double itemsPrice = itemCount * shop.getPrice();
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

    public void buy(Player seller, int amount) {
        // Implement the buy logic if needed
        // This method is currently not used in the provided code
    }

    private int extractItems(Player buyer, Chest chest, int itemCount) {
        Material material = shop.getMaterial();

        for (ItemStack chestItem : chest.getInventory().getContents()) {
            if (itemCount <= 0)
                break;

            if (chestItem != null && chestItem.getType() == material) {
                int chestAmount = chestItem.getAmount();
                int removeAmount = Math.min(itemCount, chestAmount);

                ItemStack clone = chestItem.clone();
                clone.setAmount(removeAmount);

                HashMap<Integer, ItemStack> leftover = buyer.getInventory().addItem(clone);

                if (leftover.isEmpty()) {
                    chestItem.setAmount(chestAmount - removeAmount);
                    itemCount -= removeAmount;
                } else {
                    int added = removeAmount - leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    chestItem.setAmount(chestAmount - added);
                    itemCount -= added;
                    break;
                }
            }
        }

        return itemCount;
    }

}
