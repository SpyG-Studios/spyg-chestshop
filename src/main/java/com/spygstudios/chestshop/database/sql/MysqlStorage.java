package com.spygstudios.chestshop.database.sql;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.DatabaseHandler;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.utils.FutureUtils;

import lombok.Getter;

public class MysqlStorage extends DatabaseHandler implements DataManager {

    @Getter
    private final DatabaseType databaseType;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MysqlStorage(ChestShop plugin, String host, int port, String database, String username, String password) {
        super(plugin, DatabaseType.MYSQL);
        this.databaseType = super.databaseType;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        initialize();
    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("MySQL driver not found: " + e.getMessage());
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            try {
                String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                        host, port, database);
                connection = DriverManager.getConnection(url, username, password);
                createTables();
                plugin.getLogger().info("MySQL connection established: " + host + ":" + port + "/" + database);
                Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                    loadPlayerShops(player.getUniqueId());
                });
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to connect to MySQL database: " + host + ":" + port + "/" + database);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Shop> createShop(UUID ownerId, String shopName, Location location) {
        return FutureUtils.runTaskAsync(plugin, () -> {
            String createdAt = plugin.getDataManager().getDateString();
            Shop shop = new Shop(ownerId, shopName, location, createdAt);
            shop.setSaved(false);
            return shop;
        });
    }

    @Override
    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(ownerId);
        if (player != null && player.isOnline()) {
            List<Shop> playerShops = Shop.getShops().stream()
                    .filter(shop -> shop.getOwnerId().equals(ownerId))
                    .toList();
            return CompletableFuture.completedFuture(playerShops);
        }
        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "SELECT * FROM shops WHERE owner_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                ResultSet rs = stmt.executeQuery();

                Map<Integer, Shop> shops = new HashMap<>();
                while (rs.next()) {
                    Entry<Integer, Shop> shopEntry = loadShopMapFromResult(rs);
                    if (shopEntry == null) {
                        continue;
                    }
                    shops.put(shopEntry.getKey(), shopEntry.getValue());
                }
                loadShopPlayers(shops);
                return new ArrayList<>(shops.values());
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player shops: " + e.getMessage());
                return List.of();
            }
        });
    }

    public CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId) {
        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "SELECT * FROM shops WHERE owner_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                ResultSet rs = stmt.executeQuery();

                Map<Integer, Shop> shops = new HashMap<>();
                while (rs.next()) {
                    Entry<Integer, Shop> shopEntry = loadShopMapFromResult(rs);
                    shops.put(shopEntry.getKey(), shopEntry.getValue());
                }
                loadShopPlayers(shops);
                return new ArrayList<>(shops.values());
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player shops: " + e.getMessage());
                return List.of();
            }
        });
    }

    @Override
    public void loadShopsInChunk(Chunk chunk) {
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException("Chunk must be loaded to get shops");
        }

        FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "SELECT * FROM shops WHERE world = ? AND x >= ? AND x < ? AND z >= ? AND z < ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, chunk.getWorld().getName());
                stmt.setInt(2, chunk.getX() * 16);
                stmt.setInt(3, (chunk.getX() + 1) * 16);
                stmt.setInt(4, chunk.getZ() * 16);
                stmt.setInt(5, (chunk.getZ() + 1) * 16);

                ResultSet rs = stmt.executeQuery();
                Map<Integer, Shop> shops = new HashMap<>();
                while (rs.next()) {
                    Entry<Integer, Shop> shopEntry = loadShopMapFromResult(rs);
                    shops.put(shopEntry.getKey(), shopEntry.getValue());
                }
                loadShopPlayers(shops);

                return null;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get shops in chunk: " + e.getMessage());
                return null;
            }
        });
    }

    @Override
    public void unloadShopsInChunk(Chunk chunk) {
        List<Shop> shops = Shop.getShops();
        if (shops == null || shops.isEmpty()) {
            return;
        }
        for (Shop shop : shops) {
            if (!chunk.getWorld().equals(shop.getChestLocation().getWorld())) {
                continue;
            }
            int x = shop.getChestLocation().getBlockX() >> 4;
            int z = shop.getChestLocation().getBlockZ() >> 4;
            if (x != chunk.getX() || z != chunk.getZ()) {
                continue;
            }
            if (shop.isSaved()) {
                shop.unload();
            } else {
                plugin.getDataManager().saveShop(shop);
            }
        }
    }

    @Override
    public CompletableFuture<Shop> getShop(UUID ownerId, String shopName) {
        return FutureUtils.runTaskAsync(plugin, () -> {
            Shop existingShop = Shop.getShop(ownerId, shopName);
            if (existingShop != null) {
                return existingShop;
            }
            String sql = "SELECT * FROM shops WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return null;
                }

                Entry<Integer, Shop> shopEntry = loadShopMapFromResult(rs);
                loadShopPlayers(shopEntry);
                return shopEntry.getValue();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get shop: " + e.getMessage());
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateShopPrice(UUID ownerId, String shopName, double price) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }
        String sql = "UPDATE shops SET price = ? WHERE owner_uuid = ? AND shop_name = ?";
        execute(sql, price, ownerId.toString(), shopName);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> updateShopMaterial(UUID ownerId, String shopName, Material material) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET material = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, material != null ? material.name() : null, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        return execute(shopName, ownerId.toString(), notify);
    }

    @Override
    public CompletableFuture<Boolean> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "UPDATE shops SET sold_items = ?, money_earned = ? WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, soldItems);
                stmt.setDouble(2, moneyEarned);
                stmt.setString(3, ownerId.toString());
                stmt.setString(4, shopName);
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update shop stats: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName) {
        Shop shop = Shop.getShop(ownerId, oldName);
        if (shop != null) {
            plugin.getLogger().info("Shop name updated in memory for owner: " + ownerId + ", oldName: " + oldName + ", newName: " + newName);
            return CompletableFuture.completedFuture(true);
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "UPDATE shops SET shop_name = ? WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newName);
                stmt.setString(2, ownerId.toString());
                stmt.setString(3, oldName);
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to rename shop: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName) {
        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "DELETE FROM shops WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete shop: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<List<UUID>> getShopPlayers(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getAddedPlayers());
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "SELECT player_uuid FROM shop_players WHERE shop_id = (SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                List<UUID> players = new ArrayList<>();
                while (rs.next()) {
                    players.add(UUID.fromString(rs.getString("player_uuid")));
                }
                return players;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get shop players: " + e.getMessage());
                return List.of();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> addPlayerToShop(UUID ownerId, String shopName, UUID toAdd) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            if (shop.getAddedPlayers().contains(toAdd)) {
                return CompletableFuture.completedFuture(true);
            }
            plugin.getLogger().info("Player added to shop in memory for owner: " + ownerId + ", shopName: " + shopName);
            return CompletableFuture.completedFuture(true);
        }
        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "INSERT INTO shop_players (shop_id, player_uuid) VALUES ((SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?), ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                stmt.setString(3, toAdd.toString());
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to add player to shop: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            if (!shop.getAddedPlayers().contains(toRemove)) {
                return CompletableFuture.completedFuture(true);
            }
            shop.removePlayer(toRemove);
            plugin.getLogger().info("Player removed from shop in memory for owner: " + ownerId + ", shopName: " + shopName);
            return CompletableFuture.completedFuture(true);
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "DELETE FROM shop_players WHERE shop_id = (SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?) AND player_uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                stmt.setString(3, toRemove.toString());
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove player from shop: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            shop.setSoldItems(shop.getSoldItems() + soldItems);
            plugin.getLogger().info("Sold items updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return CompletableFuture.completedFuture(true);
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "UPDATE shops SET sold_items = sold_items + ? WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, soldItems);
                stmt.setString(2, ownerId.toString());
                stmt.setString(3, shopName);
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update sold items: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            shop.setMoneyEarned(shop.getMoneyEarned() + moneyEarned);
            plugin.getLogger().info("Money earned updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return CompletableFuture.completedFuture(true);
        }

        return FutureUtils.runTaskAsync(plugin, () -> {
            String sql = "UPDATE shops SET money_earned = money_earned + ? WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDouble(1, moneyEarned);
                stmt.setString(2, ownerId.toString());
                stmt.setString(3, shopName);
                int rowsAffected = stmt.executeUpdate();
                boolean success = rowsAffected > 0;
                return success;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update money earned: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> saveShop(Shop shop) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Runnable saveTask = () -> {
            String selectSql = "SELECT 1 FROM shops WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setString(1, shop.getOwnerId().toString());
                selectStmt.setString(2, shop.getName());

                try (ResultSet rs = selectStmt.executeQuery()) {
                    boolean exists = rs.next();
                    String sql;
                    PreparedStatement stmt;

                    if (exists) {
                        sql = "UPDATE shops SET price = ?, material = ?, world = ?, x = ?, y = ?, z = ?, created_at = ?, do_notify = ? " +
                                "WHERE owner_uuid = ? AND shop_name = ?";
                        stmt = connection.prepareStatement(sql);
                        stmt.setDouble(1, shop.getPrice());
                        stmt.setString(2, shop.getMaterial() != null ? shop.getMaterial().name() : null);
                        stmt.setString(3, shop.getChestLocation().getWorld().getName());
                        stmt.setInt(4, shop.getChestLocation().getBlockX());
                        stmt.setInt(5, shop.getChestLocation().getBlockY());
                        stmt.setInt(6, shop.getChestLocation().getBlockZ());
                        stmt.setString(7, shop.getCreatedAt());
                        stmt.setBoolean(8, shop.isNotify());
                        stmt.setString(9, shop.getOwnerId().toString());
                        stmt.setString(10, shop.getName());
                    } else {
                        sql = "INSERT INTO shops (owner_uuid, shop_name, price, material, world, x, y, z, created_at, do_notify) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        stmt = connection.prepareStatement(sql);
                        stmt.setString(1, shop.getOwnerId().toString());
                        stmt.setString(2, shop.getName());
                        stmt.setDouble(3, shop.getPrice());
                        stmt.setString(4, shop.getMaterial() != null ? shop.getMaterial().name() : null);
                        stmt.setString(5, shop.getChestLocation().getWorld().getName());
                        stmt.setInt(6, shop.getChestLocation().getBlockX());
                        stmt.setInt(7, shop.getChestLocation().getBlockY());
                        stmt.setInt(8, shop.getChestLocation().getBlockZ());
                        stmt.setString(9, shop.getCreatedAt());
                        stmt.setBoolean(10, shop.isNotify());
                    }

                    int rowsAffected = stmt.executeUpdate();
                    boolean success = rowsAffected > 0;
                    if (!success) {
                        plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId());
                    }
                    future.complete(success);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId() + ": " + e.getMessage());
            }
        };
        if (!plugin.isEnabled()) {
            saveTask.run();
            return future;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, saveTask);
        return future;
    }

    public void close() {
        plugin.getLogger().info("Closing MySQL connection and saving shops...");
        for (Shop shop : Shop.getShops()) {
            if (shop.isSaved()) {
                continue;
            }
            try {
                saveShop(shop).join();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId());
                plugin.getLogger().severe("Error: " + e.getMessage());
            }
        }
        super.close();
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shops (
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                owner_uuid VARCHAR(36) NOT NULL,
                                shop_name VARCHAR(255) NOT NULL,
                                price DECIMAL(15,2) NOT NULL DEFAULT 0,
                                material VARCHAR(255),
                                world VARCHAR(100) NOT NULL,
                                x INT NOT NULL,
                                y INT NOT NULL,
                                z INT NOT NULL,
                                created_at VARCHAR(50) NOT NULL,
                                do_notify BOOLEAN NOT NULL DEFAULT FALSE,
                                sold_items INT NOT NULL DEFAULT 0,
                                money_earned DECIMAL(15,2) NOT NULL DEFAULT 0,
                                UNIQUE KEY unique_shop (owner_uuid, shop_name),
                                INDEX idx_owner (owner_uuid)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shop_players (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            shop_id INT NOT NULL,
                            player_uuid VARCHAR(36) NOT NULL,
                            FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
                            UNIQUE KEY unique_player_shop (shop_id, player_uuid),
                            INDEX idx_shop_id (shop_id),
                            INDEX idx_player_uuid (player_uuid)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
        }
    }

    private Entry<Integer, Shop> loadShopMapFromResult(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID ownerId = UUID.fromString(rs.getString("owner_uuid"));
        String shopName = rs.getString("shop_name");
        Shop existingShop = Shop.getShop(ownerId, shopName);
        if (existingShop != null) {
            return null;
        }
        double price = rs.getDouble("price");
        String materialName = rs.getString("material");
        Material material = materialName != null ? Material.getMaterial(materialName) : null;
        Location shopLocation = new Location(Bukkit.getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"));
        String createdAt = rs.getString("created_at");
        boolean notify = rs.getBoolean("do_notify");

        Shop shop = new Shop(ownerId, shopName, price, material, shopLocation, createdAt, notify, new ArrayList<>());
        return new SimpleEntry<Integer, Shop>(id, shop);
    }

    private void loadShopPlayers(Map<Integer, Shop> shops) throws SQLException {
        if (shops.isEmpty())
            return;
        String sql = "SELECT player_uuid, shop_id FROM shop_players where shop_id IN (" + String.join(",", shops.keySet().stream().map(String::valueOf).toList()) + ")";
        try (Statement playerStmt = connection.createStatement()) {
            ResultSet playerRs = playerStmt.executeQuery(sql);
            while (playerRs.next()) {
                int shopId = playerRs.getInt("shop_id");
                UUID playerUuid = UUID.fromString(playerRs.getString("player_uuid"));
                Shop shop = shops.get(shopId);
                if (shop != null) {
                    shop.addPlayer(playerUuid);
                }
            }
        }
    }

    private void loadShopPlayers(Entry<Integer, Shop> shop) throws SQLException {
        Map<Integer, Shop> shops = new HashMap<>();
        shops.put(shop.getKey(), shop.getValue());
        loadShopPlayers(shops);
    }

    @Override
    public void startSaveScheduler() {
        long interval = plugin.getConf().getInt("shops.save-interval", 60);
        if (interval <= 0)
            interval = 60;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Shop shop : Shop.getShops()) {
                if (shop.isSaved()) {
                    continue;
                }
                try {
                    boolean isSaved = saveShop(shop).join();
                    if (isSaved) {
                        shop.setSaved(true);
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId());
                    plugin.getLogger().severe("Error: " + e.getMessage());
                }
            }
        }, 0, 20L * interval);
    }

    @Override
    public CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId) {
        return FutureUtils.runTaskAsync(plugin, () -> {
            for (Shop shop : Shop.getShops()) {
                if (shop.getOwnerId() == null || !shop.getOwnerId().equals(ownerId)) {
                    continue;
                }
                if (shop.getChestLocation().isChunkLoaded()) {
                    continue;
                }
                if (shop.isSaved()) {
                    shop.unload();
                    continue;
                }
                boolean isSaved = saveShop(shop).join();
                if (isSaved) {
                    shop.setSaved(true);
                }
                shop.unload();
            }
            return true;
        });
    }

}