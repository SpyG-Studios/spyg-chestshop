package com.spygstudios.chestshop.interfaces;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;

import com.spygstudios.chestshop.shop.Shop;

public interface ShopFile {
    UUID getOwnerId();

    void setPlayers(List<UUID> players, String shopName);

    void addPlayer(UUID player, String shopName);

    void removePlayer(UUID player, String shopName);

    List<UUID> getAddedUuids(String shopName);

    Set<String> getPlayerShops();

    void removeShop(String shopName);

    void addShop(Shop shop);

    void renameShop(String shopName, String newName);

    void setMaterial(String shopName, Material material);
}
