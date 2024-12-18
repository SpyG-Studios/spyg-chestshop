package com.spygstudios.chestshop.shop;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.location.LocationUtils;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class ShopFile extends YamlManager {

    private UUID ownerId;

    public ShopFile(ChestShop plugin, Player owner) {
        this(plugin, owner.getUniqueId());
    }

    public ShopFile(ChestShop plugin, UUID ownerId) {
        super("shops/" + ownerId + ".yml", plugin);
        if (SHOPS_FILES.containsKey(ownerId)) {
            return;
        }
        set("shops", Arrays.asList());
        saveConfig();
        SHOPS_FILES.put(ownerId, this);
    }

    public Set<String> getPlayerShops() {
        return getConfigurationSection("shops").getKeys(false);
    }

    public void removeShop(String shopName) {
        for (String shop : getPlayerShops()) {
            if (shop.equalsIgnoreCase(shopName)) {
                overwriteSet("shops." + shop, null);
                saveConfig();
                return;
            }
        }
    }

    public void addShop(Shop shop) {
        set("shops." + shop.getName() + ".price", shop.getPrice());
        set("shops." + shop.getName() + ".amount", shop.getAmount());
        set("shops." + shop.getName() + ".material", shop.getMaterial() == null ? null : shop.getMaterial().name());
        set("shops." + shop.getName() + ".location", LocationUtils.fromLocation(shop.getChestLocation(), true));
        saveConfig();
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public static void loadShopFiles(ChestShop plugin) {
        File shopsFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopsFolder.exists()) {
            shopsFolder.mkdirs();
            return;
        }
        if (shopsFolder.isDirectory()) {
            for (File file : shopsFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    try {
                        UUID ownerId = UUID.fromString(file.getName().replace(".yml", ""));
                        ShopFile shopFile = new ShopFile(plugin, ownerId);
                        for (String shopName : shopFile.getPlayerShops()) {
                            String shopPath = "shops." + shopName;
                            String locationString = shopFile.getString(shopPath + ".location");
                            if (locationString == null) {
                                plugin.getLogger().warning("Invalid shop file: " + file.getName());
                                continue;
                            }
                            Location location = LocationUtils.toLocation(locationString);
                            String materialString = shopFile.getString(shopPath + ".material");

                            new Shop(ownerId, shopName, location, Material.getMaterial(materialString), shopFile.getInt(shopPath + ".amount"), shopFile.getDouble(shopPath + ".price"));
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid shop file: " + file.getName());
                    }
                }
            }
        }
    }

    public static ShopFile getShopFile(UUID ownerId) {
        return SHOPS_FILES.get(ownerId);
    }

    public static ShopFile getShopFile(Player owner) {
        return getShopFile(owner.getUniqueId());
    }

    public static void removeShopFile(UUID ownerId) {
        SHOPS_FILES.remove(ownerId);
    }

    public static void removeShopFile(Player owner) {
        removeShopFile(owner.getUniqueId());
    }

    public static Map<UUID, ShopFile> getShopsFiles() {
        return new HashMap<>(SHOPS_FILES);
    }

    private static final Map<UUID, ShopFile> SHOPS_FILES = new HashMap<>();
}
