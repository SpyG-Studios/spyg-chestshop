package com.spygstudios.chestshop.shop;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.location.LocationUtils;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

import lombok.Getter;

public class ShopFile extends YamlManager {
    @Getter
    private UUID ownerId;
    private boolean isSaved;

    public ShopFile(ChestShop plugin, Player owner) {
        this(plugin, owner.getUniqueId());
    }

    public ShopFile(ChestShop plugin, UUID ownerId) {
        super("shops/" + ownerId + ".yml", plugin);
        if (SHOPS_FILES.containsKey(ownerId)) {
            return;
        }
        set("shops", null);
        setDefaultValues(this);
        this.isSaved = true;
        this.ownerId = ownerId;
        SHOPS_FILES.put(ownerId, this);
    }

    public void setPlayers(List<UUID> players, String shopName) {
        overwriteSet("shops." + shopName + ".added-players", players.stream().map(UUID::toString).toList());
        isSaved = false;
    }

    public void addPlayer(UUID player, String shopName) {
        List<UUID> players = getAddedUuids(shopName);
        players.add(player);
        setPlayers(players, shopName);
    }

    public void removePlayer(UUID player, String shopName) {
        List<UUID> players = getAddedUuids(shopName);
        players.remove(player);
        setPlayers(players, shopName);
    }

    public List<UUID> getAddedUuids(String shopName) {
        return new ArrayList<>(getStringList("shops." + shopName + ".added-players").stream().map(UUID::fromString).toList());
    }

    public Set<String> getPlayerShops() {
        if (getConfigurationSection("shops") == null) {
            return new HashSet<>();
        }
        return getConfigurationSection("shops").getKeys(false);
    }

    public void removeShop(String shopName) {
        for (String shop : getPlayerShops()) {
            if (shop.equalsIgnoreCase(shopName)) {
                overwriteSet("shops." + shop, null);
                isSaved = false;
                return;
            }
        }
    }

    private static void setDefaultValues(ShopFile shopFile) {
        for (String shopName : shopFile.getPlayerShops()) {
            String shopPath = "shops." + shopName;
            shopFile.set(shopPath + ".price", 0);
            shopFile.set(shopPath + ".do-notify", false);
            shopFile.set(shopPath + ".sold-items", 0);
            shopFile.set(shopPath + ".money-earned", 0);
            shopFile.set(shopPath + ".created", getDateString());
            shopFile.isSaved = false;
        }
    }

    public void addShop(Shop shop) {
        String name = shop.getName();
        set("shops." + name + ".price", 0);
        set("shops." + name + ".material", null);
        set("shops." + name + ".location", LocationUtils.fromLocation(shop.getChestLocation(), true));
        set("shops." + name + ".do-notify", false);
        set("shops." + name + ".created", getDateString());
        set("shops." + name + ".added-players", new ArrayList<String>());
        isSaved = false;
    }

    public void setName(String shopName, String name) {
        set("shops." + name + ".price", getDouble("shops." + shopName + ".price", 0));
        set("shops." + name + ".material", getString("shops." + shopName + ".material", null));
        set("shops." + name + ".location", getString("shops." + shopName + ".location"));
        set("shops." + name + ".do-notify", getBoolean("shops." + shopName + ".do-notify", false));
        set("shops." + name + ".created", getString("shops." + shopName + ".created", getDateString()));
        set("shops." + name + ".added-players", getStringList("shops." + shopName + ".added-players", new ArrayList<>()));
        overwriteSet("shops." + shopName, null);
        isSaved = false;
    }

    public void setMaterial(String shopName, Material material) {
        overwriteSet("shops." + shopName + ".material", material == null ? null : material.name());
        isSaved = false;
    }

    public void setPrice(String shopName, double price) {
        overwriteSet("shops." + shopName + ".price", price);
        isSaved = false;
    }

    public void save() {
        isSaved = false;
    }

    public static String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    public static void loadShopFiles(ChestShop plugin) {
        plugin.getLogger().info("Loading shops...");
        File shopsFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopsFolder.exists()) {
            shopsFolder.mkdirs();
            plugin.getLogger().info("Shops loaded!");
            return;
        }

        for (File file : shopsFolder.listFiles()) {
            processShopFile(plugin, file);
        }
        plugin.getLogger().info("Shops loaded!");
    }

    private static void processShopFile(ChestShop plugin, File file) {
        if (!file.isFile() || !file.getName().endsWith(".yml")) {
            return;
        }
        UUID ownerId;
        try {
            ownerId = UUID.fromString(file.getName().replace(".yml", ""));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid shop file: " + file.getName() + " (invalid UUID)");
            return;
        }
        ShopFile shopFile = new ShopFile(plugin, ownerId);
        for (String shopName : shopFile.getPlayerShops()) {
            processShop(plugin, file, shopFile, shopName);
        }
    }

    private static void processShop(ChestShop plugin, File file, ShopFile shopFile, String shopName) {
        String shopPath = "shops." + shopName;
        String locationString = shopFile.getString(shopPath + ".location");
        if (locationString == null) {
            plugin.getLogger().warning("Invalid shop file: " + file.getName() + " (location is null) removing shop...");
            shopFile.removeShop(shopName);
            return;
        }
        Location location = LocationUtils.toLocation(locationString);
        if (location.getWorld() == null || ShopUtils.isDisabledWorld(location.getWorld().getName())) {
            return;
        }
        if (!location.getBlock().getType().equals(Material.CHEST)) {
            plugin.getLogger().warning("Invalid shop in: " + file.getName() + " (chest is not a chest) removing shop...");
            shopFile.removeShop(shopName);
            return;
        }
        double price = shopFile.getDouble("shops." + shopName + ".price");
        Material material = Material.getMaterial(shopFile.getString("shops." + shopName + ".material"));
        String createdAt = shopFile.getString("shops." + shopName + ".created");
        boolean isNotify = shopFile.getBoolean("shops." + shopName + ".do-notify");
        new Shop(shopFile.getOwnerId(), shopName, price, material, location, createdAt, isNotify, shopFile.getAddedUuids(shopName), shopFile);
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
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, ShopFile::saveShops, 0, 20L * plugin.getConf().getInt("shops.save-interval", 60));
    }

    public static void saveShops() {
        for (ShopFile shopFile : SHOPS_FILES.values()) {
            if (shopFile.isSaved) {
                continue;
            }
            shopFile.saveConfig();
            shopFile.isSaved = true;
        }
    }
}
