package com.spygstudios.chestshop.interfaces;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.shop.Shop;

public interface DataManager {
    CompletableFuture<Boolean> initialize();

    CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location);

    CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId);

    CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId);

    CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId);

    CompletableFuture<List<Shop>> getShopsInChunk(Chunk chunk);

    CompletableFuture<Shop> getShop(UUID ownerId, String shopName);

    CompletableFuture<Boolean> updateShopPrice(UUID ownerId, String shopName, double price);

    CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material);

    CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify);

    CompletableFuture<Boolean> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned);

    CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName);

    CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName);

    CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName);

    CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd);

    CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove);

    CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems);

    CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned);

    CompletableFuture<Boolean> saveShop(Shop shop);

    void startSaveScheduler();

    void close();

    default String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }
}
