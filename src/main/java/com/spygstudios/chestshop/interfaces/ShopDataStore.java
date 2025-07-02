package com.spygstudios.chestshop.interfaces;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.shop.Shop;

public interface ShopDataStore {
    void createShop(UUID ownerId, String shopName, Location location, String createdAt, Consumer<Integer> callback);

    void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback);

    void getShopByLocation(Location location, Consumer<Shop> callback);

    void getShop(UUID ownerId, String shopName, Consumer<Shop> callback);

    void updateShopPrice(UUID ownerId, String shopName, double price, Runnable callback);

    void updateShopMaterial(UUID ownerId, String shopName, Material material, Runnable callback);

    void updateShopNotify(UUID ownerId, String shopName, boolean notify, Runnable callback);

    void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Runnable callback);

    void renameShop(UUID ownerId, String oldName, String newName, Runnable callback);

    void deleteShop(UUID ownerId, String shopName, Runnable callback);

    void getShopPlayers(int shopId, Consumer<List<UUID>> callback);

    void addPlayerToShop(int shopId, UUID playerId, Runnable callback);

    void removePlayerFromShop(int shopId, UUID playerId, Runnable callback);

    void getShopId(UUID ownerId, String shopName, Consumer<Integer> callback);

    void initialize(Consumer<Boolean> callback);

    void close();
}
