package com.spygstudios.chestshop.shop.sqlite;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

public class SqlliteStorage implements DataManager {

    @Override
    public void initialize(Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @Override
    public void createShop(UUID ownerId, String shopName, Location location, Consumer<Shop> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createShop'");
    }

    @Override
    public void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerShops'");
    }

    @Override
    public void getShopsInChunk(Chunk chunk, Consumer<List<Shop>> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShopsInChunk'");
    }

    @Override
    public void getShop(UUID ownerId, String shopName, Consumer<Shop> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShop'");
    }

    @Override
    public void updateShopPrice(UUID ownerId, String shopName, double price, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopPrice'");
    }

    @Override
    public void updateShopMaterial(UUID ownerId, String shopName, Material material, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopMaterial'");
    }

    @Override
    public void updateShopNotify(UUID ownerId, String shopName, boolean notify, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopNotify'");
    }

    @Override
    public void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopStats'");
    }

    @Override
    public void renameShop(UUID ownerId, String oldName, String newName, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renameShop'");
    }

    @Override
    public void deleteShop(UUID ownerId, String shopName, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShop'");
    }

    @Override
    public void getShopPlayers(UUID ownerId, String shopName, Consumer<List<UUID>> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShopPlayers'");
    }

    @Override
    public void addPlayerToShop(UUID ownerId, String shopName, UUID toAdd, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addPlayerToShop'");
    }

    @Override
    public void removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removePlayerFromShop'");
    }

    @Override
    public void updateSoldItems(UUID ownerId, String shopName, int soldItems, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSoldItems'");
    }

    @Override
    public void updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned, Consumer<Boolean> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMoneyEarned'");
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

}
