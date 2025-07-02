package com.spygstudios.chestshop.shop.sql;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.database.ShopRepository;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.sqlite.SqliteShopFile;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class DatabaseShopTransactions {

    private final Shop shop;
    private final ChestShop plugin;
    private final ShopRepository repository;

    public DatabaseShopTransactions(Shop shop, SqliteShopFile shopFile) {
        this.shop = shop;
        this.plugin = ChestShop.getInstance();
        // A repository megszerzése a plugin adatbázis manager-étől
        this.repository = shopFile.getRepository();
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
                Map.of("%price%", String.valueOf(itemsPrice), "%material%", shop.getMaterial().name(),
                        "%items-left%", String.valueOf(itemsLeft), "%items-bought%", String.valueOf(itemCount)));

        // SQLite-ba mentés aszinkron módon
        repository.updateShopStats(shop.getOwnerId(), shop.getName(), itemCount, itemsPrice);

        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_SOLD.send(owner, Map.of("%price%", String.valueOf(itemsPrice), "%material%", shop.getMaterial().name(),
                    "%player-name%", buyer.getName(), "%items-left%", String.valueOf(itemsLeft), "%items-bought%", String.valueOf(itemCount)));
        }
    }

    public void buy(Player seller, int amount) {
        // TODO: buy metódus implementálása
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
