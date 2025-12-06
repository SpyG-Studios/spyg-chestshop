package com.spygstudios.chestshop.interfaces;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.shop.Shop;

public interface DataManager {
    CompletableFuture<Boolean> initialize();

    CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location);

    CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId);

    CompletableFuture<Shop> getShop(UUID ownerId, String shopName);

    CompletableFuture<Boolean> updateShopBuyPrice(UUID ownerId, String shopName, double price);

    CompletableFuture<Boolean> updateShopSellPrice(UUID ownerId, String shopName, double price);

    CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material);

    CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify);

    CompletableFuture<Boolean> updateShopBuyStats(UUID ownerId, String shopName, int boughtItems, double moneyEarned);

    CompletableFuture<Boolean> updateShopSellStats(UUID ownerId, String shopName, int soldItems, double moneyEarned);

    CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName);

    CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName);

    CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName);

    CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd);

    CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove);

    CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems);

    CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned);

    CompletableFuture<Integer> getBoughtItems(UUID ownerId, String shopName);

    CompletableFuture<Integer> getSoldItems(UUID ownerId, String shopName);

    CompletableFuture<Integer> getMoneySpent(UUID ownerId, String shopName);

    CompletableFuture<Boolean> saveShop(Shop shop);

    CompletableFuture<Boolean> setCanBuyFromPlayers(UUID ownerId, String shopName, boolean canBuy);

    CompletableFuture<Boolean> setCanSellToPlayers(UUID ownerId, String shopName, boolean canSell);

    void startSaveScheduler();

    void close();

    default String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }
}
