package com.spygstudios.chestshop.shop.sqlite;

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
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<List<Shop>> loadShopsInChunk(Chunk chunk) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Shop> getShop(UUID ownerId, String shopName) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> updateShopPrice(UUID ownerId, String shopName, double price) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public void createTables() throws SQLException {
        throw new UnsupportedOperationException("SQLite storage not implemented");
    }

    @Override
    public void startSaveScheduler() {
        throw new UnsupportedOperationException("SQLite storage not implemented");
    }

    @Override
    public CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SQLite storage not implemented"));
    }

    @Override
    public CompletableFuture<Boolean> saveShop(Shop shop) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveShop'");
    }

}
