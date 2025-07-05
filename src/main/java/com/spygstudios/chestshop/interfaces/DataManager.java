package com.spygstudios.chestshop.interfaces;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.shop.Shop;

public interface DataManager {
    void initialize(Consumer<Boolean> callback);

    void createShop(UUID ownerId, String shopName, Location location, Consumer<Shop> callback);

    void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback);

    void getShopsInChunk(Chunk chunk, Consumer<List<Shop>> callback);

    void getShop(UUID ownerId, String shopName, Consumer<Shop> callback);

    void updateShopPrice(UUID ownerId, String shopName, double price, Consumer<Boolean> callback);

    void updateShopMaterial(UUID ownerId, String shopName, Material material, Consumer<Boolean> callback);

    void updateShopNotify(UUID ownerId, String shopName, boolean notify, Consumer<Boolean> callback);

    void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Consumer<Boolean> callback);

    void renameShop(UUID ownerId, String oldName, String newName, Consumer<Boolean> callback);

    void deleteShop(UUID ownerId, String shopName, Consumer<Boolean> callback);

    void getShopPlayers(UUID ownerId, String shopName, Consumer<List<UUID>> callback);

    void addPlayerToShop(UUID ownerId, String shopName, UUID toAdd, Consumer<Boolean> callback);

    void removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove, Consumer<Boolean> callback);

    void updateSoldItems(UUID ownerId, String shopName, int soldItems, Consumer<Boolean> callback);

    void updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned, Consumer<Boolean> callback);

    void saveShop(Shop shop, Consumer<Boolean> callback);

    void startSaveScheduler();

    void close();

    default String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }
}
