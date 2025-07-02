package com.spygstudios.chestshop.shop.yaml;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.interfaces.ShopDataStore;
import com.spygstudios.chestshop.shop.Shop;

public class YamlStorage implements ShopDataStore {

    private final ChestShop plugin;

    public YamlStorage(ChestShop plugin) {
        this.plugin = plugin;
    }

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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ShopYmlFile.getShopFile(ownerId).removeShop(shopName);
            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
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

    @Override
    public void getShopId(UUID ownerId, String shopName, Consumer<Integer> callback) {
        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(-1));
    }

    @Override
    public void initialize(Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                ShopYmlFile.loadShopFiles(plugin);
                ShopYmlFile.startSaveScheduler(plugin);
                plugin.getLogger().info("YamlStorage initialized successfully.");
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to initialize YamlShopRepository: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
            }
        });
    }

    @Override
    public void close() {
        ShopYmlFile.saveShops();
    }

}
