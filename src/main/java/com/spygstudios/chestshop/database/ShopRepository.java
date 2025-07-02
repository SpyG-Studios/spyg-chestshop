package com.spygstudios.chestshop.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.interfaces.ShopFile;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.sqlite.SqliteShopFile;
import com.spygstudios.spyglib.location.LocationUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopRepository {

    private final DatabaseHandler database;
    private final ChestShop plugin;

    public void createShop(UUID ownerId, String shopName, Location location, String createdAt, Consumer<Integer> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            String sql = """
                    INSERT INTO shops (owner_uuid, shop_name, location, created_at)
                    VALUES (?, ?, ?, ?)
                    """;

            try (PreparedStatement stmt = database.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);
                stmt.setString(3, LocationUtils.fromLocation(location, true));
                stmt.setString(4, createdAt);

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int shopId = rs.getInt(1);
                        scheduler.runTask(plugin, () -> callback.accept(shopId));
                        return;
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(-1));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating shop: " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(-1));
            }
        });
    }

    public void getPlayerShops(UUID ownerId, Consumer<List<Shop>> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            List<Shop> shops = new ArrayList<>();
            String sql = "SELECT * FROM shops WHERE owner_uuid = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Shop shop = createShopFromResultSet(rs);
                        if (shop != null) {
                            shops.add(shop);
                        }
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(shops));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error loading shops for player " + ownerId + ": " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(new ArrayList<>()));
            }
        });
    }

    public void getShopByLocation(Location location, Consumer<Shop> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT * FROM shops WHERE location = ?";
            String locationString = LocationUtils.fromLocation(location, true);

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, locationString);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Shop shop = createShopFromResultSet(rs);
                        scheduler.runTask(plugin, () -> callback.accept(shop));
                        return;
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(null));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error searching for shop at location " + location + ": " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(null));
            }
        });
    }

    public void getShop(UUID ownerId, String shopName, Consumer<Shop> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT * FROM shops WHERE owner_uuid = ? AND shop_name = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Shop shop = createShopFromResultSet(rs);
                        scheduler.runTask(plugin, () -> callback.accept(shop));
                        return;
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(null));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error searching for shop " + shopName + " for player " + ownerId + ": " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(null));
            }
        });
    }

    public void updateShopPrice(UUID ownerId, String shopName, double price, Runnable callback) {
        database.executeAsync(
                "UPDATE shops SET price = ? WHERE owner_uuid = ? AND shop_name = ?",
                new Object[] { price, ownerId.toString(), shopName }, callback);
    }

    public void updateShopMaterial(UUID ownerId, String shopName, Material material, Runnable callback) {
        database.executeAsync(
                "UPDATE shops SET material = ? WHERE owner_uuid = ? AND shop_name = ?",
                new Object[] { material != null ? material.name() : null, ownerId.toString(), shopName }, callback);
    }

    public void updateShopNotify(UUID ownerId, String shopName, boolean notify, Runnable callback) {
        Object notifyValue = database.getDatabaseType() == DatabaseType.SQLITE ? (notify ? 1 : 0) : notify;
        database.executeAsync(
                "UPDATE shops SET do_notify = ? WHERE owner_uuid = ? AND shop_name = ?",
                new Object[] { notifyValue, ownerId.toString(), shopName }, callback);
    }

    public void updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned, Runnable callback) {
        database.executeAsync(
                "UPDATE shops SET sold_items = sold_items + ?, money_earned = money_earned + ? WHERE owner_uuid = ? AND shop_name = ?",
                new Object[] { soldItems, moneyEarned, ownerId.toString(), shopName }, callback);
    }

    public void renameShop(UUID ownerId, String oldName, String newName, Runnable callback) {
        database.executeAsync(
                "UPDATE shops SET shop_name = ? WHERE owner_uuid = ? AND shop_name = ?",
                new Object[] { newName, ownerId.toString(), oldName }, callback);
    }

    public void deleteShop(UUID ownerId, String shopName, Runnable callback) {
        database.executeAsync(
                "DELETE FROM shops WHERE owner_uuid = ? AND shop_name = ?",
                new Object[] { ownerId.toString(), shopName }, callback);
    }

    public void getShopPlayers(int shopId, Consumer<List<UUID>> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            List<UUID> players = new ArrayList<>();
            String sql = "SELECT player_uuid FROM shop_players WHERE shop_id = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setInt(1, shopId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        players.add(UUID.fromString(rs.getString("player_uuid")));
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(players));
            } catch (SQLException e) {
                plugin.getLogger().severe("Error loading players for shop ID " + shopId + ": " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(new ArrayList<>()));
            }
        });
    }

    public void addPlayerToShop(int shopId, UUID playerId, Runnable callback) {
        database.executeAsync(
                "INSERT OR IGNORE INTO shop_players (shop_id, player_uuid) VALUES (?, ?)",
                new Object[] { shopId, playerId.toString() }, callback);
    }

    public void removePlayerFromShop(int shopId, UUID playerId, Runnable callback) {
        database.executeAsync(
                "DELETE FROM shop_players WHERE shop_id = ? AND player_uuid = ?",
                new Object[] { shopId, playerId.toString() }, callback);
    }

    private Shop createShopFromResultSet(ResultSet rs) throws SQLException {
        UUID ownerId = UUID.fromString(rs.getString("owner_uuid"));
        String shopName = rs.getString("shop_name");
        double price = rs.getDouble("price");
        String materialName = rs.getString("material");
        Material material = materialName != null ? Material.getMaterial(materialName) : null;
        Location location = LocationUtils.toLocation(rs.getString("location"));
        String createdAt = rs.getString("created_at");

        boolean isNotify;
        if (database.getDatabaseType() == DatabaseType.SQLITE) {
            isNotify = rs.getInt("do_notify") == 1;
        } else {
            isNotify = rs.getBoolean("do_notify");
        }

        // We'll get the added players via callback now, so create shop with empty list
        // for now
        List<UUID> addedPlayers = new ArrayList<>();
        SqliteShopFile shopFile = new SqliteShopFile(plugin, ownerId, this);

        return new Shop(ownerId, shopName, price, material, location, createdAt, isNotify, addedPlayers, shopFile);
    }

    public void getAllShops(Consumer<List<Shop>> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            List<Shop> shops = new ArrayList<>();
            String sql = "SELECT * FROM shops";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Shop shop = createShopFromResultSet(rs);
                        if (shop != null) {
                            shops.add(shop);
                        }
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(shops));
            } catch (SQLException e) {
                plugin.getLogger().severe("Hiba minden shop betöltése során: " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(new ArrayList<>()));
            }
        });
    }

    public void getShopId(UUID ownerId, String shopName, Consumer<Integer> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int shopId = rs.getInt("id");
                        scheduler.runTask(plugin, () -> callback.accept(shopId));
                        return;
                    }
                }
                scheduler.runTask(plugin, () -> callback.accept(-1));
            } catch (SQLException e) {
                plugin.getLogger().severe("Hiba shop ID keresése során: " + e.getMessage());
                scheduler.runTask(plugin, () -> callback.accept(-1));
            }
        });
    }
}
