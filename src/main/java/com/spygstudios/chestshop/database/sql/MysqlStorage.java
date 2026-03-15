package com.spygstudios.chestshop.database.sql;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.DatabaseHandler;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.SqlDataManager;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.utils.FutureUtils;

public class MysqlStorage extends DatabaseHandler implements SqlDataManager {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private BukkitTask saveTask;

    public MysqlStorage(ChestShop plugin, String host, int port, String database, String username, String password) {
        super(plugin, DatabaseType.MYSQL);
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
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
        String createdAt = plugin.getDateString();
        Shop shop = new Shop(ownerId, shopName, location, createdAt);
        return CompletableFuture.completedFuture(shop);
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

                Map<Integer, ShopRow> shopDataList = new HashMap<>();
                while (rs.next()) {
                    ShopRow shopData = extractShopRow(rs);
                    if (shopData != null) {
                        shopDataList.put(shopData.id(), shopData);
                    }
                }
                loadShopPlayersData(shopDataList);

                return runSync(() -> {
                    List<Shop> shops = new ArrayList<>();
                    for (ShopRow data : shopDataList.values()) {
                        Shop shop = createShopFromRow(data);
                        if (shop != null) {
                            shops.add(shop);
                        }
                    }
                    return shops;
                });
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

                Map<Integer, ShopRow> shopDataList = new HashMap<>();
                while (rs.next()) {
                    ShopRow shopData = extractShopRow(rs);
                    if (shopData != null) {
                        shopDataList.put(shopData.id(), shopData);
                    }
                }
                loadShopPlayersData(shopDataList);

                return runSync(() -> {
                    List<Shop> shops = new ArrayList<>();
                    for (ShopRow data : shopDataList.values()) {
                        Shop shop = createShopFromRow(data);
                        if (shop != null) {
                            shops.add(shop);
                        }
                    }
                    return shops;
                });
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
                Map<Integer, ShopRow> shopDataList = new HashMap<>();
                while (rs.next()) {
                    ShopRow shopData = extractShopRow(rs);
                    if (shopData != null) {
                        shopDataList.put(shopData.id(), shopData);
                    }
                }
                loadShopPlayersData(shopDataList);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (ShopRow data : shopDataList.values()) {
                        createShopFromRow(data);
                    }
                });

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

                ShopRow shopData = extractShopRow(rs);
                if (shopData != null) {
                    Map<Integer, ShopRow> dataMap = new HashMap<>();
                    dataMap.put(shopData.id(), shopData);
                    loadShopPlayersData(dataMap);

                    return runSync(() -> createShopFromRow(shopData));
                }
                return null;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get shop: " + e.getMessage());
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateShopBuyPrice(UUID ownerId, String shopName, double buyPrice) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET buy_price = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, buyPrice, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateShopSellPrice(UUID ownerId, String shopName, double sellPrice) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET sell_price = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, sellPrice, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateShopItem(UUID ownerId, String shopName, ItemStack item) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET item = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, item != null ? plugin.bytesToString(item.serializeAsBytes()) : null, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET do_notify = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, ownerId.toString(), notify);
    }

    @Override
    public CompletableFuture<Boolean> updateShopSellStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET sold_items = ?, money_earned = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, soldItems, moneyEarned, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateShopBuyStats(UUID ownerId, String shopName, int boughtItems, double moneySpent) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET bought_items = ?, money_spent = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, boughtItems, moneySpent, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> renameShop(UUID ownerId, String oldName, String newName) {
        Shop shop = Shop.getShop(ownerId, oldName);
        if (shop != null) {
            plugin.getLogger().info("Shop name updated in memory for owner: " + ownerId + ", oldName: " + oldName + ", newName: " + newName);
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET shop_name = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, newName, ownerId.toString(), oldName);
    }

    @Override
    public CompletableFuture<Boolean> deleteShop(UUID ownerId, String shopName) {
        String sql = "DELETE FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, ownerId.toString(), shopName);
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
        String sql = "INSERT INTO shop_players (shop_id, player_uuid) VALUES ((SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?), ?)";
        return execute(sql, ownerId.toString(), shopName, toAdd.toString());
    }

    @Override
    public CompletableFuture<Boolean> removePlayerFromShop(UUID ownerId, String shopName, UUID toRemove) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            if (!shop.getAddedPlayers().contains(toRemove)) {
                return CompletableFuture.completedFuture(true);
            }
            return CompletableFuture.completedFuture(true);
        }

        String sql = "DELETE FROM shop_players WHERE shop_id = (SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?) AND player_uuid = ?";
        return execute(sql, ownerId.toString(), shopName, toRemove.toString());
    }

    @Override
    public CompletableFuture<Boolean> updateSoldItems(UUID ownerId, String shopName, int soldItems) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET sold_items = sold_items + ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, soldItems, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateMoneyEarned(UUID ownerId, String shopName, double moneyEarned) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET money_earned = money_earned + ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, moneyEarned, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> saveShop(Shop shop) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Runnable saveTask = () -> {
            String sql = """
                    INSERT INTO shops
                    (owner_uuid, shop_name, sell_price, buy_price, item, world, x, y, z,
                     created_at, do_notify, sold_items, bought_items, money_spent,
                     money_earned, can_buy, can_sell, quantity)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        sell_price = VALUES(sell_price),
                        buy_price = VALUES(buy_price),
                        item = VALUES(item),
                        world = VALUES(world),
                        x = VALUES(x),
                        y = VALUES(y),
                        z = VALUES(z),
                        do_notify = VALUES(do_notify),
                        sold_items = VALUES(sold_items),
                        bought_items = VALUES(bought_items),
                        money_spent = VALUES(money_spent),
                        money_earned = VALUES(money_earned),
                        can_buy = VALUES(can_buy),
                        can_sell = VALUES(can_sell),
                        quantity = VALUES(quantity)
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                int index = 1;

                byte[] itemData = shop.getItem() != null
                        ? shop.getItem().serializeAsBytes()
                        : null;

                stmt.setString(index++, shop.getOwnerId().toString());
                stmt.setString(index++, shop.getName());
                stmt.setDouble(index++, shop.getSellPrice());
                stmt.setDouble(index++, shop.getBuyPrice());
                stmt.setBytes(index++, itemData);

                stmt.setString(index++, shop.getChestLocation().getWorld().getName());
                stmt.setInt(index++, shop.getChestLocation().getBlockX());
                stmt.setInt(index++, shop.getChestLocation().getBlockY());
                stmt.setInt(index++, shop.getChestLocation().getBlockZ());

                stmt.setString(index++, shop.getCreatedAt());
                stmt.setBoolean(index++, shop.isNotify());
                stmt.setInt(index++, shop.getSoldItems());
                stmt.setInt(index++, shop.getBoughtItems());
                stmt.setDouble(index++, shop.getMoneySpent());
                stmt.setDouble(index++, shop.getMoneyEarned());
                stmt.setBoolean(index++, shop.acceptsCustomerPurchases());
                stmt.setBoolean(index++, shop.acceptsCustomerSales());
                stmt.setInt(index++, shop.getQuantity());

                boolean success = stmt.executeUpdate() > 0;
                if (!success) {
                    plugin.getLogger().severe(
                            "Failed to save shop: " + shop.getName() +
                                    " for owner: " + shop.getOwnerId());
                }

                future.complete(success);
            } catch (SQLException e) {
                plugin.getLogger().severe(
                        "Failed to save shop: " + shop.getName() +
                                " for owner: " + shop.getOwnerId() +
                                ": " + e.getMessage());
                future.completeExceptionally(e);
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
        if (saveTask != null) {
            saveTask.cancel();
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
                                sell_price DECIMAL(15,2) NOT NULL DEFAULT 0,
                                buy_price DECIMAL(15,2) NOT NULL DEFAULT 0,
                                item BLOB,
                                world VARCHAR(100) NOT NULL,
                                x INT NOT NULL,
                                y INT NOT NULL,
                                z INT NOT NULL,
                                created_at VARCHAR(50) NOT NULL,
                                do_notify BOOLEAN NOT NULL DEFAULT FALSE,
                                sold_items INT NOT NULL DEFAULT 0,
                                bought_items INT NOT NULL DEFAULT 0,
                                money_spent DECIMAL(15,2) NOT NULL DEFAULT 0,
                                money_earned DECIMAL(15,2) NOT NULL DEFAULT 0,
                                can_buy BOOLEAN NOT NULL DEFAULT TRUE,
                                can_sell BOOLEAN NOT NULL DEFAULT FALSE,
                                quantity INT NOT NULL DEFAULT 1,
                                UNIQUE KEY unique_shop (owner_uuid, shop_name),
                                INDEX idx_owner (owner_uuid)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """);

            stmt.execute("ALTER TABLE shops ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1");

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

    private ShopRow extractShopRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID ownerId = UUID.fromString(rs.getString("owner_uuid"));
        String shopName = rs.getString("shop_name");
        Shop existingShop = Shop.getShop(ownerId, shopName);
        if (existingShop != null) {
            return null;
        }

        return new ShopRow(
                id,
                ownerId,
                shopName,
                rs.getDouble("sell_price"),
                rs.getDouble("buy_price"),
                rs.getBytes("item"),
                rs.getString("world"),
                rs.getInt("x"),
                rs.getInt("y"),
                rs.getInt("z"),
                rs.getString("created_at"),
                rs.getBoolean("do_notify"),
                rs.getInt("sold_items"),
                rs.getInt("bought_items"),
                rs.getDouble("money_spent"),
                rs.getDouble("money_earned"),
                rs.getBoolean("can_buy"),
                rs.getBoolean("can_sell"),
                rs.getInt("quantity"),
                new ArrayList<>());
    }

    private Shop createShopFromRow(ShopRow data) {
        Shop existingShop = Shop.getShop(data.ownerId(), data.shopName());
        if (existingShop != null) {
            return existingShop;
        }

        ItemStack item = data.item() != null ? ItemStack.deserializeBytes(data.item()) : null;
        Location shopLocation = new Location(
                Bukkit.getWorld(data.world()),
                data.x(),
                data.y(),
                data.z());

        return new Shop(
                data.ownerId(),
                data.shopName(),
                data.sellPrice(),
                data.buyPrice(),
                item,
                shopLocation,
                data.createdAt(),
                data.doNotify(),
                data.canSell(),
                data.canBuy(),
                data.playersWithAccess(),
                data.quantity());
    }

    private void loadShopPlayersData(Map<Integer, ShopRow> shopDataMap) throws SQLException {
        if (shopDataMap.isEmpty()) {
            return;
        }
        String sql = "SELECT player_uuid, shop_id FROM shop_players where shop_id IN (" + String.join(",", shopDataMap.keySet().stream().map(String::valueOf).toList()) + ")";
        try (Statement playerStmt = connection.createStatement()) {
            ResultSet playerRs = playerStmt.executeQuery(sql);
            while (playerRs.next()) {
                int shopId = playerRs.getInt("shop_id");
                UUID playerUuid = UUID.fromString(playerRs.getString("player_uuid"));
                ShopRow shopData = shopDataMap.get(shopId);
                if (shopData != null) {
                    shopData.playersWithAccess().add(playerUuid);
                }
            }
        }
    }

    private <T> T runSync(Callable<T> task) {
        CompletableFuture<T> f = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                f.complete(task.call());
            } catch (Exception e) {
                f.completeExceptionally(e);
            }
        });
        return f.join();
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

        this.saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
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
        }, 0L, 20L * interval);
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

    @Override
    public CompletableFuture<Boolean> updateBoughtItems(UUID ownerId, String shopName, int boughtItems) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET bought_items = bought_items + ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, boughtItems, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateMoneySpent(UUID ownerId, String shopName, double moneySpent) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }

        String sql = "UPDATE shops SET money_spent = money_spent + ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, moneySpent, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Integer> getBoughtItems(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getBoughtItems());
        }
        String sql = "SELECT bought_items FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        return FutureUtils.runTaskAsync(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("bought_items");
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get bought items: " + e.getMessage());
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getSoldItems(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getSoldItems());
        }
        String sql = "SELECT sold_items FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        return FutureUtils.runTaskAsync(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("sold_items");
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get sold items: " + e.getMessage());
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Double> getMoneySpent(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getMoneySpent());
        }
        String sql = "SELECT money_spent FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        return FutureUtils.runTaskAsync(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("money_spent");
                }
                return 0.0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get money spent: " + e.getMessage());
                return 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Double> getMoneyEarned(UUID ownerId, String shopName) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(shop.getMoneyEarned());
        }
        String sql = "SELECT money_earned FROM shops WHERE owner_uuid = ? AND shop_name = ?";
        return FutureUtils.runTaskAsync(plugin, () -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("money_earned");
                }
                return 0.0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get money earned: " + e.getMessage());
                return 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> setCanBuyFromPlayers(UUID ownerId, String shopName, boolean canBuy) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }
        String sql = "UPDATE shops SET can_buy = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, canBuy, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> setCanSellToPlayers(UUID ownerId, String shopName, boolean canSell) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }
        String sql = "UPDATE shops SET can_sell = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, canSell, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<Boolean> updateShopQuantity(UUID ownerId, String shopName, int quantity) {
        Shop shop = Shop.getShop(ownerId, shopName);
        if (shop != null) {
            return CompletableFuture.completedFuture(true);
        }
        String sql = "UPDATE shops SET quantity = ? WHERE owner_uuid = ? AND shop_name = ?";
        return execute(sql, quantity, ownerId.toString(), shopName);
    }

    @Override
    public CompletableFuture<List<Shop>> getAllShops() {
        return FutureUtils.runTaskAsync(plugin, () -> {
            List<Shop> allShops = new ArrayList<>();

            try {
                PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT owner_uuid FROM shops");
                ResultSet rs = stmt.executeQuery();

                List<UUID> ownerIds = new ArrayList<>();
                while (rs.next()) {
                    try {
                        ownerIds.add(UUID.fromString(rs.getString("owner_uuid")));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in database: " + rs.getString("owner_uuid"));
                    }
                }
                rs.close();
                stmt.close();

                for (UUID ownerId : ownerIds) {
                    List<Shop> playerShops = loadPlayerShops(ownerId).join();
                    allShops.addAll(playerShops);
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Error during loading shops from SQL: " + e.getMessage());
                e.printStackTrace();
            }

            return allShops;
        });
    }

}