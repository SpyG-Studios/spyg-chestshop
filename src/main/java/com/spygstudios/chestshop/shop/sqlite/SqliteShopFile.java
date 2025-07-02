package com.spygstudios.chestshop.shop.sqlite;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.ShopRepository;
import com.spygstudios.chestshop.interfaces.ShopFile;
import com.spygstudios.chestshop.shop.Shop;

import lombok.Getter;

public class SqliteShopFile implements ShopFile {

    @Getter
    private final UUID ownerId;
    private final ShopRepository repository;
    private static final Map<UUID, SqliteShopFile> SHOP_FILES = new HashMap<>();

    public SqliteShopFile(ChestShop plugin, UUID ownerId, ShopRepository repository) {
        this.ownerId = ownerId;
        this.repository = repository;
        SHOP_FILES.put(ownerId, this);
    }

    public SqliteShopFile(ChestShop plugin, Player owner, ShopRepository repository) {
        this(plugin, owner.getUniqueId(), repository);
    }

    public void setPlayers(List<UUID> players, String shopName) {
        // Jelenlegi játékosok eltávolítása és újak hozzáadása
        repository.getShopId(ownerId, shopName, shopId -> {
            if (shopId != -1) {
                // Meglévő játékosok törlése
                repository.getShopPlayers(shopId, currentPlayers -> {
                    for (UUID player : currentPlayers) {
                        repository.removePlayerFromShop(shopId, player, () -> {
                        });
                    }

                    // Új játékosok hozzáadása
                    for (UUID player : players) {
                        repository.addPlayerToShop(shopId, player, () -> {
                        });
                    }
                });
            }
        });
    }

    public void addPlayer(UUID player, String shopName) {
        repository.getShopId(ownerId, shopName, shopId -> {
            if (shopId != -1) {
                repository.addPlayerToShop(shopId, player, () -> {
                });
            }
        });
    }

    public void removePlayer(UUID player, String shopName) {
        int shopId = repository.getShopId(ownerId, shopName).join();
        if (shopId != -1) {
            repository.removePlayerFromShop(shopId, player);
        }
    }

    public List<UUID> getAddedUuids(String shopName) {
        int shopId = repository.getShopId(ownerId, shopName).join();
        if (shopId != -1) {
            return repository.getShopPlayers(shopId).join();
        }
        return List.of();
    }

    public Set<String> getPlayerShops() {
        return repository.getPlayerShops(ownerId).join()
                .stream()
                .map(Shop::getName)
                .collect(Collectors.toSet());
    }

    public void removeShop(String shopName) {
        repository.deleteShop(ownerId, shopName);
    }

    public void addShop(Shop shop) {
        repository.createShop(ownerId, shop.getName(), shop.getChestLocation(), getDateString());
    }

    public void renameShop(String shopName, String newName) {
        repository.renameShop(ownerId, shopName, newName);
    }

    public void setMaterial(String shopName, Material material) {
        repository.updateShopMaterial(ownerId, shopName, material);
    }

    public void setPrice(String shopName, double price) {
        repository.updateShopPrice(ownerId, shopName, price);
    }

    public void markUnsaved() {
        // SQLite-ban automatikus mentés
    }

    // Kompatibilitási metódusok a YML rendszerrel
    public void overwriteSet(String path, Object value) {
        // Parse path to extract shop info and update accordingly
        String[] parts = path.split("\\.");
        if (parts.length >= 3 && "shops".equals(parts[0])) {
            String shopName = parts[1];
            String property = parts[2];

            switch (property) {
                case "do-notify":
                    repository.updateShopNotify(ownerId, shopName, (Boolean) value);
                    break;
                case "sold-items":
                case "money-earned":
                    // Ezeket a ShopTransactions kezeli külön
                    break;
            }
        }
    }

    public int getInt(String path) {
        // Implementáció a statisztikákhoz - ezt a ShopTransactions használja
        return 0; // Placeholder - statisztikák kezelése más módon történik SQLite-ban
    }

    public double getDouble(String path) {
        // Implementáció a statisztikákhoz
        return 0.0; // Placeholder
    }

    public boolean getBoolean(String path) {
        return false; // Placeholder
    }

    public static String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    public static SqliteShopFile getShopFile(UUID ownerId) {
        return SHOP_FILES.get(ownerId);
    }

    public static SqliteShopFile getShopFile(Player owner) {
        return getShopFile(owner.getUniqueId());
    }

    public static void removeShopFile(UUID ownerId) {
        SHOP_FILES.remove(ownerId);
    }

    public static void removeShopFile(Player owner) {
        removeShopFile(owner.getUniqueId());
    }

    public static Map<UUID, SqliteShopFile> getShopsFiles() {
        return new HashMap<>(SHOP_FILES);
    }

    public ShopRepository getRepository() {
        return repository;
    }
}
