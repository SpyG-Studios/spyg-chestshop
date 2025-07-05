package com.spygstudios.chestshop.shop.sql;

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
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitScheduler;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.DatabaseHandler;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

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

        initialize(success -> {
            if (success) {
                plugin.getLogger().info("MySQL initialized successfully.");
            } else {
                plugin.getLogger().severe("Failed to initialize MySQL.");
            }
        });
    }

    @Override
    public void initialize(Consumer<Boolean> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                        host, port, database);
                connection = DriverManager.getConnection(url, username, password);
                createTables();
                plugin.getLogger().info("MySQL connection established: " + host + ":" + port + "/" + database);
                scheduler.runTask(plugin, () -> callback.accept(true));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to connect to MySQL database: " + host + ":" + port + "/" + database);
                scheduler.runTask(plugin, () -> callback.accept(false));
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
        shop.setSaved(false);
        callback.accept(shop);
    }

    @Override
    public void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(ownerId);
        if (player != null && player.isOnline()) {
            List<Shop> playerShops = Shop.getShops().stream()
                    .filter(shop -> shop.getOwnerId().equals(ownerId))
                    .toList();
            callback.accept(playerShops);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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

                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new ArrayList<>(shops.values())));
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player shops: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(List.of()));
            }
        });

    }

    @Override
    public void getShopsInChunk(Chunk chunk, Consumer<List<Shop>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException("Chunk must be loaded to get shops");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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

                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new ArrayList<>(shops.values())));

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get shops in chunk: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(List.of()));
            }
        });
    }

    @Override
    public void getShop(UUID ownerId, String shopName, Consumer<Shop> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Shop existingShop = Shop.getShop(ownerId, shopName);
            if (existingShop != null) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(existingShop));
                return;
            }
            String sql = "SELECT * FROM shops WHERE owner_uuid = ? AND shop_name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
                    return;
                }

                Entry<Integer, Shop> shopEntry = loadShopMapFromResult(rs);
                loadShopPlayers(shopEntry);
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(shopEntry.getValue()));
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get shop: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
            }
        });
    }

    @Override
    public void updateShopPrice(UUID ownerId, String shopName, double price, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Shop price updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }
        String sql = "UPDATE shops SET price = ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { price, ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Shop price updated in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to update shop price for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void updateShopMaterial(UUID ownerId, String shopName, Material material, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Shop price updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }
        String sql = "UPDATE shops SET material = ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { material != null ? material.name() : null, ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Shop material updated in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to update shop material for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void updateShopNotify(UUID ownerId, String shopName, boolean notify, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Shop notify updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }
        String sql = "UPDATE shops SET do_notify = ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { true, ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Shop notify updated in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to update shop notify for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Shop stats updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }
        String sql = "UPDATE shops SET sold_items = ?, money_earned = ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { soldItems, moneyEarned, ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Shop stats updated in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to update shop stats for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));

        });
    }

    @Override
    public void renameShop(UUID ownerId, String oldName, String newName, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, oldName);
        if (shop != null) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Shop name updated in memory for owner: " + ownerId + ", oldName: " + oldName + ", newName: " + newName);
            return;
        }
        String sql = "UPDATE shops SET shop_name = ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { newName, ownerId.toString(), oldName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Shop name updated in database for owner: " + ownerId + ", oldName: " + oldName + ", newName: " + newName);
                return;
            }
            plugin.getLogger().severe("Failed to update shop stats for owner: " + ownerId + ", oldName: " + oldName + ", newName: " + newName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void deleteShop(UUID ownerId, String shopName, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            callback.accept(true);
            plugin.getLogger().info("Shop deleted in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }
        String sql = "DELETE FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Shop deleted from database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to delete shop for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void getShopPlayers(UUID ownerId, String shopName, Consumer<List<UUID>> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(shop.getAddedPlayers()));
            return;
        }

        String sql = "SELECT player_uuid FROM shop_players WHERE shop_id = (SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ownerId.toString());
            stmt.setString(2, shopName);
            ResultSet rs = stmt.executeQuery();

            List<UUID> players = new ArrayList<>();
            while (rs.next()) {
                players.add(UUID.fromString(rs.getString("player_uuid")));
            }
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(players));
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get shop players: " + e.getMessage());
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(List.of()));
        }
    }

    @Override
    public void addPlayerToShop(UUID ownerId, String shopName, UUID toAdd, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            if (shop.getAddedPlayers().contains(toAdd)) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                return;
            }
            shop.addPlayer(toAdd);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Player added to shop in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }

        String sql = "INSERT INTO shop_players (shop_id, player_uuid) VALUES ((SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?), ?)";
        Object[] params = new Object[] { ownerId.toString(), shopName, toAdd.toString() };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Player added to shop in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to add player to shop for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            if (!shop.getAddedPlayers().contains(toRemove)) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                return;
            }
            shop.removePlayer(toRemove);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Player removed from shop in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }

        String sql = "DELETE FROM shop_players WHERE shop_id = (SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?) AND player_uuid = ?";
        Object[] params = new Object[] { ownerId.toString(), shopName, toRemove.toString() };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Player removed from shop in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to remove player from shop for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void updateSoldItems(UUID ownerId, String shopName, int soldItems, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            shop.setSoldItems(shop.getSoldItems() + soldItems);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Sold items updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }

        String sql = "UPDATE shops SET sold_items = sold_items + ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { soldItems, ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Sold items updated in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to update sold items for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            shop.setMoneyEarned(shop.getMoneyEarned() + moneyEarned);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            plugin.getLogger().info("Money earned updated in memory for owner: " + ownerId + ", shopName: " + shopName);
            return;
        }

        String sql = "UPDATE shops SET money_earned = money_earned + ? WHERE owner_uuid = ? AND shop_name = ?";
        Object[] params = new Object[] { moneyEarned, ownerId.toString(), shopName };

        executeAsync(sql, params, success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                plugin.getLogger().info("Money earned updated in database for owner: " + ownerId + ", shopName: " + shopName);
                return;
            }
            plugin.getLogger().severe("Failed to update money earned for owner: " + ownerId + ", shopName: " + shopName);
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        });
    }

    @Override
    public void saveShop(Shop shop, Consumer<Boolean> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        String selectSql = "SELECT 1 FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
            selectStmt.setString(1, shop.getOwnerId().toString());
            selectStmt.setString(2, shop.getName());

            try (ResultSet rs = selectStmt.executeQuery()) {
                boolean exists = rs.next();

                String sql = null;
                Object[] params = null;

                if (exists) {
                    sql = "UPDATE shops SET price = ?, material = ?, location = ?, world = ?, x = ?, y = ?, z = ?, created_at = ?, do_notify = ? " +
                            "WHERE owner_uuid = ? AND shop_name = ?";
                    params = new Object[] { shop.getPrice(), shop.getMaterial() != null ? shop.getMaterial().name() : null, shop.getChestLocation().serialize().toString(),
                            shop.getChestLocation().getWorld().getName(), shop.getChestLocation().getBlockX(), shop.getChestLocation().getBlockY(), shop.getChestLocation().getBlockZ(),
                            shop.getCreatedAt(), shop.isNotify(), shop.getOwnerId().toString(), shop.getName()
                    };
                } else {
                    sql = "INSERT INTO shops (owner_uuid, shop_name, price, material, location, world, x, y, z, created_at, do_notify) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    params = new Object[] { shop.getOwnerId().toString(), shop.getName(), shop.getPrice(), shop.getMaterial() != null ? shop.getMaterial().name() : null,
                            shop.getChestLocation().serialize().toString(), shop.getChestLocation().getWorld().getName(), shop.getChestLocation().getBlockX(), shop.getChestLocation().getBlockY(),
                            shop.getChestLocation().getBlockZ(), shop.getCreatedAt(), shop.isNotify()
                    };
                }

                executeSync(sql, params, success -> {
                    if (!success) {
                        plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId());
                    }
                    callback.accept(success);
                });

            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId());
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
        }
    }

    public void close() {
        plugin.getLogger().info("Closing MySQL connection and saving shops...");
        for (Shop shop : Shop.getShops()) {
            if (shop.isSaved()) {
                continue;
            }
            try {
                saveShop(shop, success -> {
                    if (success) {
                        shop.setSaved(true);
                    }
                });
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
            return new SimpleEntry<Integer, Shop>(id, existingShop);
        }
        double price = rs.getDouble("price");
        String materialName = rs.getString("material");
        Material material = materialName != null ? Material.getMaterial(materialName) : null;
        Location shopLocation = new Location(
                Bukkit.getWorld(rs.getString("world")),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"));
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
                    saveShop(shop, success -> {
                        if (success) {
                            shop.setSaved(true);
                        }
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to save shop: " + shop.getName() + " for owner: " + shop.getOwnerId());
                    plugin.getLogger().severe("Error: " + e.getMessage());
                }
            }
        }, 0, 20L * interval);
    }

}
