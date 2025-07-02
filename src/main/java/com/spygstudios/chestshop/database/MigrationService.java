package com.spygstudios.chestshop.database;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Material;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.sqlite.SqliteShopFile;
import com.spygstudios.chestshop.shop.yml.ShopYmlFile;
import com.spygstudios.spyglib.location.LocationUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrationService {

    private final ChestShop plugin;
    private final ShopRepository repository;

    public CompletableFuture<Boolean> migrateFromYml() {
        return CompletableFuture.supplyAsync(() -> {

            try {
                plugin.getLogger().info("Kezdődik a migráció YML-ről SQLite-ra...");

                File shopsFolder = new File(plugin.getDataFolder(), "shops");
                if (!shopsFolder.exists()) {
                    plugin.getLogger().info("Nincs shops mappa, nincs mit migrálni.");
                    return true;
                }

                File[] ymlFiles = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (ymlFiles == null || ymlFiles.length == 0) {
                    plugin.getLogger().info("Nincsenek YML fájlok, nincs mit migrálni.");
                    return true;
                }

                int migratedShops = 0;
                int migratedPlayers = 0;

                for (File ymlFile : ymlFiles) {
                    try {
                        UUID ownerId = UUID.fromString(ymlFile.getName().replace(".yml", ""));
                        ShopYmlFile oldShopFile = new ShopYmlFile(plugin, ownerId);

                        Set<String> shopNames = oldShopFile.getPlayerShops();

                        for (String shopName : shopNames) {
                            try {
                                migrateShop(oldShopFile, ownerId, shopName);
                                migratedShops++;

                                // Játékosok migrálása
                                List<UUID> players = oldShopFile.getAddedUuids(shopName);
                                int shopId = repository.getShopId(ownerId, shopName).join();
                                if (shopId != -1) {
                                    for (UUID playerId : players) {
                                        repository.addPlayerToShop(shopId, playerId).join();
                                        migratedPlayers++;
                                    }
                                }

                            } catch (Exception e) {
                                plugin.getLogger().warning("Hiba shop migrálása során: " + shopName + " - " + e.getMessage());
                            }
                        }

                    } catch (Exception e) {
                        plugin.getLogger().warning("Hiba fájl feldolgozása során: " + ymlFile.getName() + " - " + e.getMessage());
                    }
                }

                plugin.getLogger().info("Migration completed! Migrated shops: " + migratedShops + ", Migrated players: " + migratedPlayers);

                // Biztonsági másolat készítése
                createBackup(shopsFolder);

                return true;

            } catch (Exception e) {
                plugin.getLogger().severe("Error during migration: " + e.getMessage());
                return false;
            }
        });
    }

    private void migrateShop(ShopYmlFile oldShopFile, UUID ownerId, String shopName) {
        String shopPath = "shops." + shopName;

        // Alapadatok kinyerése
        double price = oldShopFile.getDouble(shopPath + ".price", 0);
        String materialName = oldShopFile.getString(shopPath + ".material");
        Material material = materialName != null ? Material.getMaterial(materialName) : null;
        String locationString = oldShopFile.getString(shopPath + ".location");
        Location location = LocationUtils.toLocation(locationString);
        String createdAt = oldShopFile.getString(shopPath + ".created", SqliteShopFile.getDateString());
        boolean doNotify = oldShopFile.getBoolean(shopPath + ".do-notify", false);
        int soldItems = oldShopFile.getInt(shopPath + ".sold-items", 0);
        double moneyEarned = oldShopFile.getDouble(shopPath + ".money-earned", 0);

        // Shop létrehozása SQLite-ban
        int shopId = repository.createShop(ownerId, shopName, location, createdAt).join();

        if (shopId != -1) {
            // További adatok frissítése
            if (material != null) {
                repository.updateShopMaterial(ownerId, shopName, material).join();
            }
            repository.updateShopPrice(ownerId, shopName, price).join();
            repository.updateShopNotify(ownerId, shopName, doNotify).join();

            // Statisztikák frissítése
            if (soldItems > 0 || moneyEarned > 0) {
                repository.updateShopStats(ownerId, shopName, soldItems, moneyEarned).join();
            }
        }
    }

    private void createBackup(File shopsFolder) {
        try {
            File backupFolder = new File(plugin.getDataFolder(), "shops_backup_yml");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            File[] ymlFiles = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (ymlFiles != null) {
                for (File ymlFile : ymlFiles) {
                    File backupFile = new File(backupFolder, ymlFile.getName());
                    java.nio.file.Files.copy(ymlFile.toPath(), backupFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }

            plugin.getLogger().info("Successfully created backup of YML files in: " + backupFolder.getAbsolutePath());

        } catch (Exception e) {
            plugin.getLogger().warning("Error creating backup of YML files: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> shouldMigrate() {
        return CompletableFuture.supplyAsync(() -> {
            // Ellenőrzi, hogy van-e YML fájl és nincs-e már SQLite adat
            File shopsFolder = new File(plugin.getDataFolder(), "shops");
            if (!shopsFolder.exists()) {
                return false;
            }

            File[] ymlFiles = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (ymlFiles == null || ymlFiles.length == 0) {
                return false;
            }

            // Ellenőrzi, hogy üres-e az SQLite adatbázis
            List<Shop> existingShops = repository.getAllShops().join();
            return existingShops.isEmpty();
        });
    }
}
