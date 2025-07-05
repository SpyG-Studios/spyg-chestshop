package com.spygstudios.chestshop.shop.yaml;

import java.util.ArrayList;
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

        initialize().thenAccept(success -> {
            if (success) {
                plugin.getLogger().info("YamlStorage initialized successfully.");
            } else {
                plugin.getLogger().severe("Failed to initialize YamlStorage.");
            }
        });
    }

    @Override
    public CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location) {
        return CompletableFuture.supplyAsync(() -> {
            String createdAt = plugin.getDataManager().getDateString();
            Shop shop = new Shop(ownerId, shopName, location, createdAt);
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.addShop(shop);
            return shop;
        });
    }

    @Override
    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        return CompletableFuture.supplyAsync(() -> Shop.getShops(ownerId));
    }

    @Override
    public CompletableFuture<List<Shop>> getShopsInChunk(Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            List<Shop> shops = new ArrayList<>();
            for (Shop shop : Shop.getShops()) {
                if (shop.getChestLocation().getWorld() != null && shop.getChestLocation().getWorld().equals(chunk.getWorld())) {
                    if (shop.getChestLocation().getChunk().equals(chunk)) {
                        shops.add(shop);
                    }
                }
            }
            return shops;
        });
    }

    @Override
    public CompletableFuture<Shop> getShop(UUID ownerId, String shopName) {
        return CompletableFuture.supplyAsync(() -> {
            Shop shop = Shop.getShop(ownerId, shopName);
            if (shop != null) {
                return shop;
            } else {
                return YamlShopFile.loadShop(plugin, shopName, null, shopName);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateShopPrice(UUID ownerId, String shopName, double price) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.setPrice(shopName, price);
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.setMaterial(shopName, material);
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.overwriteSet("shops." + shopName + ".do-notify", notify);
            shopFile.markUnsaved();
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            String shopPath = "shops." + shopName;
            shopFile.overwriteSet(shopPath + ".sold-items", shopFile.getInt(shopPath + ".sold-items") + soldItems);
            shopFile.markUnsaved();
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            String shopPath = "shops." + shopName;
            shopFile.overwriteSet(shopPath + ".money-earned", shopFile.getDouble(shopPath + ".money-earned") + moneyEarned);
            shopFile.markUnsaved();
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        return updateMoneyEarned(ownerId, shopName, moneyEarned)
                .thenCompose(result1 -> updateSoldItems(ownerId, shopName, soldItems))
                .thenApply(result2 -> true);
    }

    @Override
    public CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.renameShop(oldName, newName);
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile.getShopFile(ownerId).removeShop(shopName);
            return true;
        });
    }

    @Override
    public CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            List<UUID> players = shopFile.getAddedUuids(shopName);
            if (players != null) {
                return players;
            } else {
                Bukkit.getLogger().warning("No players found for shop: " + shopName);
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd) {
        return getShop(ownerId, shopName).thenApply(shop -> {
            if (shop == null) {
                Bukkit.getLogger().warning("Shop not found with while adding player. Shopname: " + shopName);
                return false;
            }

            YamlShopFile shopFile = YamlShopFile.getShopFile(shop.getOwnerId());
            shopFile.addPlayer(toAdd, shop.getName());
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove) {
        return getShop(ownerId, shopName).thenApply(shop -> {
            if (shop == null) {
                Bukkit.getLogger().warning("Shop not found for owner: " + ownerId + ", shop name: " + shopName);
                return false;
            }

            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.removePlayer(toRemove, shopName);
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YamlShopFile.loadShopFiles(plugin);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void close() {
        YamlShopFile.saveShopFiles();
    }

    @Override
    public CompletableFuture<Boolean> saveShop(Shop shop) {
        return CompletableFuture.supplyAsync(() -> {
            YamlShopFile shopFile = YamlShopFile.getShopFile(shop.getOwnerId());
            if (shopFile == null) {
                return false;
            }

            YamlShopFile.saveShopFile(shopFile);
            return true;
        });
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
