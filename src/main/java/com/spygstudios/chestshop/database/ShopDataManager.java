package com.spygstudios.chestshop.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.sql.MysqlManager;
import com.spygstudios.chestshop.database.sqlite.SqLiteManager;
import com.spygstudios.chestshop.services.MigrationService;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.chestshop.shop.sqlite.SqliteShopFile;

import lombok.Getter;

public class ShopDataManager {

    @Getter
    private final ShopRepository repository;

    private static final Map<UUID, SqliteShopFile> SHOP_FILES = new HashMap<>();
    private DatabaseHandler db;

    public ShopDataManager(ChestShop plugin) {
        this.db = new SqLiteManager(plugin);
        this.repository = new ShopRepository(db, plugin);
    }

    public ShopDataManager(ChestShop plugin, String host, int port, String database, String username, String password) {
        this.db = new MysqlManager(plugin, host, port, database, username, password);
        this.repository = new ShopRepository(db, plugin);
    }

    public CompletableFuture<Boolean> initialize() {
        return db.initialize().thenCompose(success -> {
            if (success) {
                return loadAllShops();
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    private CompletableFuture<Boolean> loadAllShops() {
        return repository.getAllShops().thenApply(shops -> {
            db.plugin.getLogger().info("Loading " + db.getDatabaseType() + " shops...");

            for (Shop shop : shops) {
                Location location = shop.getChestLocation();
                if (location.getWorld() == null || ShopUtils.isDisabledWorld(location.getWorld().getName())) {
                    continue;
                }
                if (!location.getBlock().getType().equals(Material.CHEST)) {
                    db.plugin.getLogger().warning("Érvénytelen shop helyszín (nem láda): " + shop.getName() + " - törlés...");
                    repository.deleteShop(shop.getOwnerId(), shop.getName());
                    continue;
                }
            }

            db.plugin.getLogger().info("SQLite shopok betöltve! Összesen: " + shops.size());
            return true;
        });
    }

    public SqliteShopFile getShopFile(UUID ownerId) {
        SqliteShopFile shopFile = SHOP_FILES.get(ownerId);
        if (shopFile == null) {
            shopFile = new SqliteShopFile(db.plugin, ownerId, repository);
            SHOP_FILES.put(ownerId, shopFile);
        }
        return shopFile;
    }

    public SqliteShopFile getShopFile(Player owner) {
        return getShopFile(owner.getUniqueId());
    }

    public void removeShopFile(UUID ownerId) {
        SHOP_FILES.remove(ownerId);
    }

    public void removeShopFile(Player owner) {
        removeShopFile(owner.getUniqueId());
    }

    public Map<UUID, SqliteShopFile> getShopsFiles() {
        return new HashMap<>(SHOP_FILES);
    }

    public CompletableFuture<Shop> getShopByLocation(Location location) {
        return repository.getShopByLocation(location);
    }

    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        return repository.getPlayerShops(ownerId);
    }

    // public void startSaveScheduler() {
    // String dbType = db.getDatabaseType().name();
    // db.plugin.getLogger().info(dbType + " módban nincs szükség mentési
    // ütemezőre");
    // }

    public void saveShops() {
        // Adatbázisban minden változás automatikusan mentődik
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public CompletableFuture<Boolean> forceMigration() {
        return new MigrationService(db.plugin, repository).migrateFromYml();
    }

    public CompletableFuture<Integer> getTotalShopsCount() {
        return repository.getAllShops().thenApply(List::size);
    }

    public CompletableFuture<Map<UUID, Integer>> getShopsCountByPlayer() {
        return repository.getAllShops().thenApply(shops -> {
            Map<UUID, Integer> counts = new HashMap<>();
            for (Shop shop : shops) {
                counts.merge(shop.getOwnerId(), 1, Integer::sum);
            }
            return counts;
        });
    }
}
