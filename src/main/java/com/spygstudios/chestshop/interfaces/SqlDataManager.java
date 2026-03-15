package com.spygstudios.chestshop.interfaces;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;

import com.spygstudios.chestshop.shop.Shop;

public interface SqlDataManager extends DataManager {

    record ShopRow(
            int id,
            UUID ownerId,
            String shopName,
            double sellPrice,
            double buyPrice,
            byte[] item,
            String world,
            int x,
            int y,
            int z,
            String createdAt,
            boolean doNotify,
            int soldItems,
            int boughtItems,
            double moneySpent,
            double moneyEarned,
            boolean canBuy,
            boolean canSell,
            int quantity,
            List<UUID> playersWithAccess) {
    }

    void loadShopsInChunk(Chunk chunk);

    void unloadShopsInChunk(Chunk chunk);

    CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId);

    CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId);

}
