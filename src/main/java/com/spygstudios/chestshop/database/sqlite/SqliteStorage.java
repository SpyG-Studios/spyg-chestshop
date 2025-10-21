package com.spygstudios.chestshop.database.sqlite;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.DatabaseHandler;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

public class SqliteStorage extends DatabaseHandler implements DataManager {

    public SqliteStorage(ChestShop plugin) {
        super(plugin, DatabaseType.SQLITE);
    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @Override
    public CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createShop'");
    }

    @Override
    public CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadPlayerShops'");
    }

    @Override
    public CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unloadPlayerShops'");
    }

    @Override
    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayerShops'");
    }

    @Override
    public void loadShopsInChunk(Chunk chunk) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadShopsInChunk'");
    }

    @Override
    public void unloadShopsInChunk(Chunk chunk) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unloadShopsInChunk'");
    }

    @Override
    public CompletableFuture<Shop> getShop(UUID ownerId, String shopName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShop'");
    }

    @Override
    public CompletableFuture<Boolean> updateShopPrice(UUID ownerId, String shopName, double price) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopPrice'");
    }

    @Override
    public CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopMaterial'");
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopNotify'");
    }

    @Override
    public CompletableFuture<Boolean> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShopStats'");
    }

    @Override
    public CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renameShop'");
    }

    @Override
    public CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShop'");
    }

    @Override
    public CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShopPlayers'");
    }

    @Override
    public CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addPlayerToShop'");
    }

    @Override
    public CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removePlayerFromShop'");
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSoldItems'");
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMoneyEarned'");
    }

    @Override
    public CompletableFuture<Boolean> saveShop(Shop shop) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveShop'");
    }

    @Override
    public void startSaveScheduler() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startSaveScheduler'");
    }

    @Override
    public void createTables() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTables'");
    }

}
