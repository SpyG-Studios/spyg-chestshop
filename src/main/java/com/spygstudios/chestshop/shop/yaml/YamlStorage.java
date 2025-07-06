package com.spygstudios.chestshop.shop.yaml;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

import lombok.Getter;

public class YamlStorage implements DataManager {

    @Getter
    private final DatabaseType databaseType;
    private final ChestShop plugin;

    public YamlStorage(ChestShop plugin) {
        this.plugin = plugin;
        databaseType = DatabaseType.YAML;

        boolean initialized = plugin.getDataManager().initialize().join();
        if (initialized) {
            plugin.getLogger().info("YamlStorage initialized successfully.");
        } else {
            plugin.getLogger().severe("Failed to initialize YamlStorage.");
        }
    }

    @Override
    public CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location) {
        String createdAt = plugin.getDataManager().getDateString();
        Shop shop = new Shop(ownerId, shopName, location, createdAt);
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.addShop(shop);
        return CompletableFuture.completedFuture(shop);
    }

    @Override
    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        List<Shop> shops = Shop.getShops(ownerId);
        return CompletableFuture.completedFuture(shops);
    }

    @Override
    public void loadShopsInChunk(Chunk chunk) {
        // This method is not used in YamlStorage, as shops are loaded on
        // initialization.
    }

    @Override
    public void unloadShopsInChunk(Chunk chunk) {
        // This method is not used in YamlStorage, as shops are loaded on
        // initialization.
    }

    @Override
    public CompletableFuture<Shop> getShop(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop);
        } else {
            Shop loadedShop = YamlShopFile.loadShop(plugin, shopName, null, shopName);
            return CompletableFuture.completedFuture(loadedShop);
        }
    }

    @Override
    public CompletableFuture<Boolean> updateShopPrice(UUID ownerId, String shopName, double price) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setPrice(shopName, price);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setMaterial(shopName, material);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.overwriteSet("shops." + shopName + ".do-notify", notify);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.overwriteSet(shopPath + ".sold-items", shopFile.getInt(shopPath + ".sold-items") + soldItems);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.overwriteSet(shopPath + ".money-earned", shopFile.getDouble(shopPath + ".money-earned") + moneyEarned);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        return updateMoneyEarned(ownerId, shopName, moneyEarned)
                .thenCompose(moneyUpdated -> updateSoldItems(ownerId, shopName, soldItems))
                .thenApply(soldItemsUpdated -> true);
    }

    @Override
    public CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.renameShop(oldName, newName);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName) {
        YamlShopFile.getShopFile(ownerId).removeShop(shopName);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        List<UUID> players = shopFile.getAddedUuids(shopName);
        if (players != null) {
            return CompletableFuture.completedFuture(players);
        } else {
            Bukkit.getLogger().warning("No players found for shop: " + shopName);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Override
    public CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd) {
        Shop shop = getShop(ownerId, shopName).join();
        if (shop == null) {
            Bukkit.getLogger().warning("Shop not found with while adding player. Shopname: " + shopName);
            return CompletableFuture.completedFuture(false);
        }

        YamlShopFile shopFile = YamlShopFile.getShopFile(shop.getOwnerId());
        shopFile.addPlayer(toAdd, shop.getName());
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove) {
        Shop shop = getShop(ownerId, shopName).join();
        if (shop == null) {
            Bukkit.getLogger().warning("Shop not found for owner: " + ownerId + ", shop name: " + shopName);
            return CompletableFuture.completedFuture(false);
        }

        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.removePlayer(toRemove, shopName);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        try {
            YamlShopFile.loadShopFiles(plugin);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    public void close() {
        YamlShopFile.saveShopFiles();
    }

    @Override
    public CompletableFuture<Boolean> saveShop(Shop shop) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(shop.getOwnerId());
        if (shopFile == null) {
            return CompletableFuture.completedFuture(false);
        }

        YamlShopFile.saveShopFile(shopFile);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void startSaveScheduler() {
        long interval = plugin.getConf().getInt("shops.save-interval", 60);
        if (interval <= 0)
            interval = 60;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, YamlShopFile::saveShopFiles, 0, 20L * interval);
    }

    @Override
    public CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId) {
        // This method is not used in YamlStorage, as shops are loaded on
        // initialization.
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId) {
        // This method is not used in YamlStorage, as shops are loaded on
        // initialization.
        return CompletableFuture.completedFuture(true);
    }

}
