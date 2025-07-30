package com.spygstudios.chestshop.shop;

import java.util.HashMap;
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
        // Check if shop has selling enabled
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

    public void buy(Player seller, int amount) {
        // Check if shop has buying enabled
        if (!shop.acceptsCustomerSales()) {
            return;
        }
        
        Material material = shop.getMaterial();
        
        // Check if player has the items to sell
        int playerItemCount = InventoryUtils.countItems(seller.getInventory(), material);
        if (playerItemCount < amount) {
            Message.NOT_ENOUGH_ITEMS.send(seller, Map.of("%material%", material.name(), "%amount%", String.valueOf(amount)));
            return;
        }
        
        // Check if shop chest has space
        Chest chest = (Chest) shop.getChestLocation().getBlock().getState();
        if (!hasChestSpace(chest, material, amount)) {
            Message.SHOP_CHEST_FULL.send(seller);
            // Notify owner about chest being full
            if (shop.isNotify()) {
                Player owner = Bukkit.getPlayer(shop.getOwnerId());
                if (owner != null) {
                    Message.SHOP_CHEST_FULL_OWNER.send(owner, Map.of("%player-name%", seller.getName(), "%material%", material.name(), "%amount%", String.valueOf(amount)));
                }
            }
            return;
        }
        
        double itemsPrice = amount * shop.getCustomerSalePrice();
        Economy economy = plugin.getEconomy();
        
        // Check if shop owner has enough money
        if (economy.getBalance(Bukkit.getOfflinePlayer(shop.getOwnerId())) < itemsPrice) {
            Message.SHOP_OWNER_NO_MONEY.send(seller);
            // Notify owner about insufficient funds
            if (shop.isNotify()) {
                Player owner = Bukkit.getPlayer(shop.getOwnerId());
                if (owner != null) {
                    Message.SHOP_OWNER_NO_MONEY_OWNER.send(owner, Map.of("%player-name%", seller.getName(), "%material%", material.name(), "%price%", String.valueOf(itemsPrice)));
                }
            }
            return;
        }
        
        // Remove items from player inventory
        int removedItems = removeItemsFromPlayer(seller, material, amount);
        
        // Add items to chest
        addItemsToChest(chest, material, removedItems);
        
        // Handle money transaction
        EconomyResponse withdrawResponse = economy.withdrawPlayer(Bukkit.getOfflinePlayer(shop.getOwnerId()), itemsPrice);
        if (withdrawResponse.transactionSuccess()) {
            economy.depositPlayer(seller, itemsPrice);
        }
        
        // Update statistics
        shopFile.overwriteSet("shops." + shop.getName() + ".bought-items", shopFile.getInt("shops." + shop.getName() + ".bought-items") + removedItems);
        shopFile.save();
        
        // Send messages
        Message.SHOP_SOLD_TO.send(seller, Map.of("%price%", String.valueOf(itemsPrice), "%material%", material.name(), "%items-sold%", String.valueOf(removedItems)));
        
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (shop.isNotify() && owner != null) {
            Message.SHOP_BOUGHT_FROM.send(owner, Map.of("%price%", String.valueOf(itemsPrice), "%material%", material.name(), "%player-name%", seller.getName(), "%items-bought%", String.valueOf(removedItems)));
        }
    }
    
    private boolean hasChestSpace(Chest chest, Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        int remainingAmount = amount;
        
        for (ItemStack item : chest.getInventory().getContents()) {
            if (remainingAmount <= 0) break;
            
            if (item == null) {
                remainingAmount -= maxStackSize;
            } else if (item.getType() == material && item.getAmount() < maxStackSize) {
                remainingAmount -= (maxStackSize - item.getAmount());
            }
        }
        
        return remainingAmount <= 0;
    }
    
    private int removeItemsFromPlayer(Player player, Material material, int amount) {
        int remainingToRemove = amount;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (remainingToRemove <= 0) break;
            
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                int removeAmount = Math.min(remainingToRemove, itemAmount);
                
                item.setAmount(itemAmount - removeAmount);
                remainingToRemove -= removeAmount;
            }
        }
        
        return amount - remainingToRemove;
    }
    
    private void addItemsToChest(Chest chest, Material material, int amount) {
        int remainingToAdd = amount;
        int maxStackSize = material.getMaxStackSize();
        
        // First, try to add to existing stacks
        for (ItemStack item : chest.getInventory().getContents()) {
            if (remainingToAdd <= 0) break;
            
            if (item != null && item.getType() == material && item.getAmount() < maxStackSize) {
                int canAdd = Math.min(remainingToAdd, maxStackSize - item.getAmount());
                item.setAmount(item.getAmount() + canAdd);
                remainingToAdd -= canAdd;
            }
        }
        
        // Then, add to empty slots
        for (int i = 0; i < chest.getInventory().getSize() && remainingToAdd > 0; i++) {
            ItemStack slot = chest.getInventory().getItem(i);
            
            if (slot == null) {
                int addAmount = Math.min(remainingToAdd, maxStackSize);
                chest.getInventory().setItem(i, new ItemStack(material, addAmount));
                remainingToAdd -= addAmount;
            }
        }
    }

}
