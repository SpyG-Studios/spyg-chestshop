package com.spygstudios.chestshop.shop;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
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
    private static boolean toSave = false;
    private UUID ownerId;

    public ShopFile(ChestShop plugin, Player owner) {
        this(plugin, owner.getUniqueId());
    }

    public ShopFile(ChestShop plugin, UUID ownerId) {
        super("shops/" + ownerId + ".yml", plugin);
        if (SHOPS_FILES.containsKey(ownerId)) {
            return;
        }
        set("shops", null);
        SHOPS_FILES.put(ownerId, this);
        toSave = true;
    }

    public Set<String> getPlayerShops() {
        if (getConfigurationSection("shops") == null) {
            return new HashSet<String>();
        }
        return getConfigurationSection("shops").getKeys(false);
    }

    public void removeShop(String shopName) {
        for (String shop : getPlayerShops()) {
            if (shop.equalsIgnoreCase(shopName)) {
                overwriteSet("shops." + shop, null);
                toSave = true;
                return;
            }
        }
    }

    public void addShop(Shop shop) {
        set("shops." + shop.getName() + ".price", shop.getPrice());
        set("shops." + shop.getName() + ".amount", shop.getAmount());
        set("shops." + shop.getName() + ".material", shop.getMaterial() == null ? null : shop.getMaterial().name());
        set("shops." + shop.getName() + ".location", LocationUtils.fromLocation(shop.getChestLocation(), true));
        set("shops." + shop.getName() + ".do-notify", shop.doNotify());
        set("shops." + shop.getName() + ".created", getDateString());
        toSave = true;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    private static void setDefaultValues(ShopFile shopFile) {
        for (String shopName : shopFile.getPlayerShops()) {
            String shopPath = "shops." + shopName;
            shopFile.set(shopPath + ".price", 0);
            shopFile.set(shopPath + ".amount", 0);
            shopFile.set(shopPath + ".do-notify", false);
            shopFile.set(shopPath + ".created", getDateString());
        }
        toSave = true;
    }

    private static String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    public static void loadShopFiles(ChestShop plugin) {
        File shopsFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopsFolder.exists()) {
            shopsFolder.mkdirs();
            return;
        }
        for (File file : shopsFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                try {
                    UUID ownerId = UUID.fromString(file.getName().replace(".yml", ""));
                    ShopFile shopFile = new ShopFile(plugin, ownerId);
                    setDefaultValues(shopFile);
                    for (String shopName : shopFile.getPlayerShops()) {
                        String shopPath = "shops." + shopName;
                        String locationString = shopFile.getString(shopPath + ".location");
                        if (locationString == null) {
                            plugin.getLogger().warning("Invalid shop file: " + file.getName());
                            continue;
                        }
                        Location location = LocationUtils.toLocation(locationString);
                        if (Shop.isDisabledWorld(location.getWorld())) {
                            continue;
                        }
                        String materialString = shopFile.getString(shopPath + ".material");

                        new Shop(ownerId, shopName, location, Material.getMaterial(materialString), shopFile.getInt(shopPath + ".amount"), shopFile.getDouble(shopPath + ".price"),
                                shopFile.getBoolean(shopPath + ".do-notify"));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid shop file: " + file.getName());
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

    public static void startSaveScheduler(ChestShop plugin) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            saveShops();
        }, 0, 20 * plugin.getConf().getInt("shops.save-interval", 60));
    }

    public static void saveShops() {
        if (toSave) {
            for (ShopFile shopFile : SHOPS_FILES.values()) {
                shopFile.saveConfig();
            }
            toSave = false;
        }
    }
}
