package com.spygstudios.chestshop.shop.sqlite;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.interfaces.ShopDataStore;
import com.spygstudios.chestshop.shop.Shop;

public class SqlliteStorage implements ShopDataStore {

    @Override
    public void createShop(UUID ownerId, String shopName, Location location, String createdAt, Consumer<Integer> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createShop'");
    }

    @Override
    public void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerShops'");
    }

    @Override
    public void getShopByLocation(Location location, Consumer<Shop> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShopByLocation'");
    }

    @Override
    public void getShop(UUID ownerId, String shopName, Consumer<Shop> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShop'");
    }

    @Override
    public void updateShopPrice(UUID ownerId, String shopName, double price, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopPrice'");
    }

    @Override
    public void updateShopMaterial(UUID ownerId, String shopName, Material material, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopMaterial'");
    }

    @Override
    public void updateShopNotify(UUID ownerId, String shopName, boolean notify, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopNotify'");
    }

    @Override
    public void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopStats'");
    }

    @Override
    public void renameShop(UUID ownerId, String oldName, String newName, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renameShop'");
    }

    @Override
    public void deleteShop(UUID ownerId, String shopName, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShop'");
    }

    @Override
    public void getShopPlayers(int shopId, Consumer<List<UUID>> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShopPlayers'");
    }

    @Override
    public void addPlayerToShop(int shopId, UUID playerId, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addPlayerToShop'");
    }

    @Override
    public void removePlayerFromShop(int shopId, UUID playerId, Runnable callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removePlayerFromShop'");
    }

    public void getAllShops(Consumer<List<Shop>> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllShops'");
    }

    @Override
    public void getShopId(UUID ownerId, String shopName, Consumer<Integer> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShopId'");
    }

    @Override
    public void initialize(Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

}
