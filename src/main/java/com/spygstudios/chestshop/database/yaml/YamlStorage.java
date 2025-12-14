package com.spygstudios.chestshop.database.yaml;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

import lombok.Getter;

public class YamlStorage implements DataManager {

    @Getter
    private final DatabaseType databaseType;
    private final ChestShop plugin;
    private BukkitTask saveTask;

    public YamlStorage(ChestShop plugin) {
        this.databaseType = DatabaseType.YAML;
        this.plugin = plugin;
        this.initialize().join();
    }

    @Override
    public CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location) {
        String createdAt = plugin.getDateString();
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
    public CompletableFuture<Boolean> updateShopBuyPrice(UUID ownerId, String shopName, double price) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setBuyPrice(shopName, price);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopSellPrice(UUID ownerId, String shopName, double price) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setSellPrice(shopName, price);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopItem(UUID ownerId, String shopName, ItemStack item) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setItem(shopName, item);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.set("shops." + shopName + ".do-notify", notify);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.set(shopPath + ".sold-items", shopFile.getInt(shopPath + ".sold-items") + soldItems);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateBoughtItems(UUID ownerId, String shopName, int boughtItems) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.set(shopPath + ".bought-items", shopFile.getInt(shopPath + ".bought-items") + boughtItems);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.set(shopPath + ".money-earned", shopFile.getDouble(shopPath + ".money-earned") + moneyEarned);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateMoneySpent(UUID ownerId, String shopName, double moneySpent) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.set(shopPath + ".money-spent", shopFile.getDouble(shopPath + ".money-spent") + moneySpent);
        shopFile.markUnsaved();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopSellStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        return updateMoneyEarned(ownerId, shopName, moneyEarned)
                .thenCompose(moneyUpdated -> updateSoldItems(ownerId, shopName, soldItems))
                .thenApply(soldItemsUpdated -> true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopBuyStats(UUID ownerId, String shopName, int boughtItems, double moneyEarned) {
        return updateMoneyEarned(ownerId, shopName, moneyEarned)
                .thenCompose(moneyUpdated -> updateSoldItems(ownerId, shopName, boughtItems))
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
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
    }

    public void close() {
        YamlShopFile.saveShopFiles();
        if (saveTask != null) {
            saveTask.cancel();
        }
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
        if (!plugin.isEnabled()) {
            return;
        }
        long interval = plugin.getConf().getInt("shops.save-interval", 60);
        if (interval <= 0) {
            interval = 60;
        }

        this.saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, YamlShopFile::saveShopFiles, 0L, 20L * interval);
    }

    @Override
    public CompletableFuture<Integer> getSoldItems(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getSoldItems());
        } else {
            return CompletableFuture.completedFuture(0);
        }
    }

    @Override
    public CompletableFuture<Integer> getBoughtItems(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getBoughtItems());
        } else {
            return CompletableFuture.completedFuture(0);
        }
    }

    @Override
    public CompletableFuture<Double> getMoneySpent(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getMoneySpent());
        } else {
            return CompletableFuture.completedFuture(0.0);
        }
    }

    @Override
    public CompletableFuture<Double> getMoneyEarned(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getMoneyEarned());
        } else {
            return CompletableFuture.completedFuture(0.0);
        }
    }

    @Override
    public CompletableFuture<Boolean> setCanBuyFromPlayers(UUID ownerId, String shopName, boolean canBuy) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setCanBuy(shopName, canBuy);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> setCanSellToPlayers(UUID ownerId, String shopName, boolean canSell) {
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setCanSell(shopName, canSell);
        return CompletableFuture.completedFuture(true);
    }

}
