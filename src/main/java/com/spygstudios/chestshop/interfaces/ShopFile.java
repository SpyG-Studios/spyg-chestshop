package com.spygstudios.chestshop.interfaces;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;

import com.spygstudios.chestshop.shop.Shop;

public interface ShopFile {

    void setPlayers(List<UUID> players, String shopName);

    void addPlayer(UUID player, String shopName);

    void removePlayer(UUID player, String shopName);

    List<UUID> getAddedUuids(String shopName);

    Set<String> getPlayerShops();

    void removeShop(String shopName);

    void addShop(Shop shop);

    void setName(String shopName, String name);

    void setMaterial(String shopName, Material material);

    void setPrice(String shopName, double price);

    void save();

    void overwriteSet(String path, Object value);

    int getInt(String path);

    double getDouble(String path);

    boolean getBoolean(String path);

    UUID getOwnerId();
}
