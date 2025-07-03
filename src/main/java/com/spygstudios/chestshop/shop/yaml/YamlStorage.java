package com.spygstudios.chestshop.shop.yaml;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

public class YamlStorage implements DataManager {

    private final ChestShop plugin;

    public YamlStorage(ChestShop plugin) {
        this.plugin = plugin;
        initialize(success -> {
            if (success) {
                plugin.getLogger().info("YamlStorage initialized successfully.");
            } else {
                plugin.getLogger().severe("Failed to initialize YamlStorage.");
            }
        });
    }

    @Override
    public void createShop(UUID ownerId, String shopName, Location location, Consumer<Shop> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        String createdAt = plugin.getDataManager().getDateString();
        Shop shop = new Shop(ownerId, shopName, location, createdAt);
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.addShop(shop);

        callback.accept(shop);
    }

    @Override
    public void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        List<Shop> shops = Shop.getShops(ownerId);
        callback.accept(shops);
    }

    @Override
    public void getShopsInChunk(Location location, Consumer<List<Shop>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

    }

    @Override
    public void getShop(UUID ownerId, String shopName, Consumer<Shop> callback) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            callback.accept(shop);
        } else {
            callback.accept(YamlShopFile.loadShop(plugin, shopName, null, shopName));
        }
    }

    @Override
    public void updateShopPrice(UUID ownerId, String shopName, double price, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setPrice(shopName, price);
        callback.accept(true);
    }

    @Override
    public void updateShopMaterial(UUID ownerId, String shopName, Material material, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.setMaterial(shopName, material);
        callback.accept(true);
    }

    @Override
    public void updateShopNotify(UUID ownerId, String shopName, boolean notify, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.overwriteSet("shops." + shopName + ".do-notify", notify);
        shopFile.markUnsaved();
        callback.accept(true);
    }

    @Override
    public void updateSoldItems(UUID ownerId, String shopName, int soldItems, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.overwriteSet(shopPath + ".sold-items", shopFile.getInt(shopPath + ".sold-items") + soldItems);
        shopFile.markUnsaved();
        callback.accept(true);
    }

    @Override
    public void updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        String shopPath = "shops." + shopName;
        shopFile.overwriteSet(shopPath + ".money-earned", shopFile.getDouble(shopPath + ".money-earned") + moneyEarned);
        shopFile.markUnsaved();
        callback.accept(true);
    }

    @Override
    public void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        updateMoneyEarned(ownerId, shopName, moneyEarned, callback);
        updateSoldItems(ownerId, shopName, soldItems, callback);
        callback.accept(true);
    }

    @Override
    public void renameShop(UUID ownerId, String oldName, String newName, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        shopFile.renameShop(oldName, newName);
        callback.accept(true);
    }

    @Override
    public void deleteShop(UUID ownerId, String shopName, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        YamlShopFile.getShopFile(ownerId).removeShop(shopName);
        callback.accept(true);
    }

    @Override
    public void getShopPlayers(UUID ownerId, String shopName, Consumer<List<UUID>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
        List<UUID> players = shopFile.getAddedUuids(shopName);
        if (players != null) {
            callback.accept(players);
        } else {
            callback.accept(List.of());
            Bukkit.getLogger().warning("No players found for shop: " + shopName);
        }
    }

    @Override
    public void addPlayerToShop(UUID ownerId, String shopName, UUID toAdd, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        getShop(ownerId, shopName, shop -> {
            if (shop == null) {
                callback.accept(false);
                Bukkit.getLogger().warning("Shop not found with while adding player. Shopname: " + shopName);
                return;
            }

            YamlShopFile shopFile = YamlShopFile.getShopFile(shop.getOwnerId());
            shopFile.addPlayer(toAdd, shop.getName());
            callback.accept(true);
        });
    }

    @Override
    public void removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        getShop(ownerId, shopName, shop -> {
            if (shop == null) {
                callback.accept(false);
                Bukkit.getLogger().warning("Shop not found for owner: " + ownerId + ", shop name: " + shopName);
                return;
            }

            YamlShopFile shopFile = YamlShopFile.getShopFile(ownerId);
            shopFile.removePlayer(toRemove, shopName);
            callback.accept(true);
        });
    }

    @Override
    public void initialize(Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                YamlShopFile.loadShopFiles(plugin);
                YamlShopFile.startSaveScheduler(plugin);
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
            }
        });
    }

    @Override
    public void close() {
        YamlShopFile.saveShops();
    }

}
