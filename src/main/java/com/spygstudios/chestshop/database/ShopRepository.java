package com.spygstudios.chestshop.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.enums.DatabaseType;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.sqlite.SqliteShopFile;
import com.spygstudios.spyglib.location.LocationUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopRepository {

    private final DatabaseHandler database;
    private final ChestShop plugin;

    public CompletableFuture<Integer> createShop(UUID ownerId, String shopName, Location location, String createdAt) {
        return CompletableFuture.supplyAsync(() -> {
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
                        return rs.getInt(1);
                    }
                }
                return -1;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error creating shop: " + e.getMessage());
                return -1;
            }
        });
    }

    public CompletableFuture<List<Shop>> getPlayerShops(UUID ownerId) {
        return CompletableFuture.supplyAsync(() -> {
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
            } catch (SQLException e) {
                plugin.getLogger().severe("Error loading shops for player " + ownerId + ": " + e.getMessage());
            }

            return shops;
        });
    }

    public CompletableFuture<Shop> getShopByLocation(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM shops WHERE location = ?";
            String locationString = LocationUtils.fromLocation(location, true);

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, locationString);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return createShopFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error searching for shop at location " + location + ": " + e.getMessage());
            }

            return null;
        });
    }

    public CompletableFuture<Shop> getShop(UUID ownerId, String shopName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM shops WHERE owner_uuid = ? AND shop_name = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return createShopFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error searching for shop " + shopName + " for player " + ownerId + ": " + e.getMessage());
            }

            return null;
        });
    }

    public CompletableFuture<Void> updateShopPrice(UUID ownerId, String shopName, double price) {
        return database.executeAsync(
                "UPDATE shops SET price = ? WHERE owner_uuid = ? AND shop_name = ?",
                price, ownerId.toString(), shopName);
    }

    public CompletableFuture<Void> updateShopMaterial(UUID ownerId, String shopName, Material material) {
        return database.executeAsync(
                "UPDATE shops SET material = ? WHERE owner_uuid = ? AND shop_name = ?",
                material != null ? material.name() : null, ownerId.toString(), shopName);
    }

    public CompletableFuture<Void> updateShopNotify(UUID ownerId, String shopName, boolean notify) {
        Object notifyValue = database.getDatabaseType() == DatabaseType.SQLITE ? (notify ? 1 : 0) : notify;

        return database.executeAsync(
                "UPDATE shops SET do_notify = ? WHERE owner_uuid = ? AND shop_name = ?",
                notifyValue, ownerId.toString(), shopName);
    }

    public CompletableFuture<Void> updateShopStats(UUID ownerId, String shopName, int soldItems, double moneyEarned) {
        return database.executeAsync(
                "UPDATE shops SET sold_items = sold_items + ?, money_earned = money_earned + ? WHERE owner_uuid = ? AND shop_name = ?",
                soldItems, moneyEarned, ownerId.toString(), shopName);
    }

    public CompletableFuture<Void> renameShop(UUID ownerId, String oldName, String newName) {
        return database.executeAsync(
                "UPDATE shops SET shop_name = ? WHERE owner_uuid = ? AND shop_name = ?",
                newName, ownerId.toString(), oldName);
    }

    public CompletableFuture<Void> deleteShop(UUID ownerId, String shopName) {
        return database.executeAsync(
                "DELETE FROM shops WHERE owner_uuid = ? AND shop_name = ?",
                ownerId.toString(), shopName);
    }

    public CompletableFuture<List<UUID>> getShopPlayers(int shopId) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> players = new ArrayList<>();
            String sql = "SELECT player_uuid FROM shop_players WHERE shop_id = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setInt(1, shopId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        players.add(UUID.fromString(rs.getString("player_uuid")));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error loading players for shop ID " + shopId + ": " + e.getMessage());
            }

            return players;
        });
    }

    public CompletableFuture<Void> addPlayerToShop(int shopId, UUID playerId) {
        return database.executeAsync(
                "INSERT OR IGNORE INTO shop_players (shop_id, player_uuid) VALUES (?, ?)",
                shopId, playerId.toString());
    }

    public CompletableFuture<Void> removePlayerFromShop(int shopId, UUID playerId) {
        return database.executeAsync(
                "DELETE FROM shop_players WHERE shop_id = ? AND player_uuid = ?",
                shopId, playerId.toString());
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

        int shopId = rs.getInt("id");
        List<UUID> addedPlayers = getShopPlayers(shopId).join();

        SqliteShopFile shopFile = new SqliteShopFile(plugin, ownerId, this);

        return new Shop(ownerId, shopName, price, material, location, createdAt, isNotify, addedPlayers, shopFile);
    }

    public CompletableFuture<List<Shop>> getAllShops() {
        return CompletableFuture.supplyAsync(() -> {
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
            } catch (SQLException e) {
                plugin.getLogger().severe("Hiba minden shop betöltése során: " + e.getMessage());
            }

            return shops;
        });
    }

    public CompletableFuture<Integer> getShopId(UUID ownerId, String shopName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT id FROM shops WHERE owner_uuid = ? AND shop_name = ?";

            try (PreparedStatement stmt = database.prepareStatement(sql)) {
                stmt.setString(1, ownerId.toString());
                stmt.setString(2, shopName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Hiba shop ID keresése során: " + e.getMessage());
            }

            return -1;
        });
    }
}
