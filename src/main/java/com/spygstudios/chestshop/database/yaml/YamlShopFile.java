package com.spygstudios.chestshop.database.yaml;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.spyglib.location.LocationUtils;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

import lombok.Getter;

public class YamlShopFile extends YamlManager {
    @Getter
    private UUID ownerId;
    private boolean isSaved;
    private static final Map<UUID, YamlShopFile> SHOPS_FILES = new HashMap<>();

    public YamlShopFile(ChestShop plugin, Player owner) {
        this(plugin, owner.getUniqueId());
    }

    public YamlShopFile(ChestShop plugin, UUID ownerId) {
        super("shops/" + ownerId + ".yml", plugin);
        if (SHOPS_FILES.containsKey(ownerId)) {
            return;
        }
        setOrDefault("shops", null);
        setDefaultValues(this);
        this.isSaved = true;
        this.ownerId = ownerId;
        SHOPS_FILES.put(ownerId, this);
    }

    public void setPlayers(List<UUID> players, String shopName) {
        set(getPath(shopName, ".added-players"), players.stream().map(UUID::toString).toList());
        isSaved = false;
    }

    public void addPlayer(UUID player, String shopName) {
        List<UUID> players = getAddedUuids(shopName);
        if (!players.contains(player)) {
            players.add(player);
            setPlayers(players, shopName);
        }
    }

    public void removePlayer(UUID player, String shopName) {
        List<UUID> players = getAddedUuids(shopName);
        players.remove(player);
        setPlayers(players, shopName);
    }

    public List<UUID> getAddedUuids(String shopName) {
        return new ArrayList<>(getStringList(getPath(shopName, ".added-players")).stream().map(UUID::fromString).toList());
    }

    public Set<String> getPlayerShops() {
        return getConfigurationSection("shops") != null
                ? getConfigurationSection("shops").getKeys(false)
                : Collections.emptySet();
    }

    public void removeShop(String shopName) {
        if (!getPlayerShops().contains(shopName)) {
            return;
        }
        set("shops." + shopName, null);
        isSaved = false;
    }

    private static void setDefaultValues(YamlShopFile shopFile) {
        for (String shopName : shopFile.getPlayerShops()) {
            String shopPath = "shops." + shopName;
            shopFile.setOrDefault(shopPath + ".price", 0);
            shopFile.setOrDefault(shopPath + ".sell-price", 0);
            shopFile.setOrDefault(shopPath + ".buy-price", 0);
            shopFile.setOrDefault(shopPath + ".do-notify", false);
            shopFile.setOrDefault(shopPath + ".can-sell", true);
            shopFile.setOrDefault(shopPath + ".can-buy", false);
            shopFile.setOrDefault(shopPath + ".sold-items", 0);
            shopFile.setOrDefault(shopPath + ".money-earned", 0);
            shopFile.setOrDefault(shopPath + ".created", getDateString());
            shopFile.isSaved = false;
        }
    }

    public void addShop(Shop shop) {
        setOrDefault(getPath(shop.getName(), ".price"), 0);
        setOrDefault(getPath(shop.getName(), ".material"), null);
        setOrDefault(getPath(shop.getName(), ".location"), LocationUtils.fromLocation(shop.getChestLocation(), true));
        setOrDefault(getPath(shop.getName(), ".do-notify"), false);
        setOrDefault(getPath(shop.getName(), ".created"), getDateString());
        setOrDefault(getPath(shop.getName(), ".added-players"), new ArrayList<String>());
        isSaved = false;
    }

    public void renameShop(String oldName, String newName) {
        ConfigurationSection section = getConfigurationSection("shops." + oldName);
        if (section != null) {
            setOrDefault("shops." + newName, section);
            set("shops." + oldName, null);
            isSaved = false;
        }
    }

    public void setMaterial(String shopName, Material material) {
        String matName = material != null ? material.name() : null;
        set(getPath(shopName, ".material"), matName);
        isSaved = false;
    }

    public void setBuyPrice(String shopName, double price) {
        set(getPath(shopName, ".buy-price"), price);
        isSaved = false;
    }

    public void setSellPrice(String shopName, double price) {
        set(getPath(shopName, ".sell-price"), price);
        isSaved = false;
    }

    public void markUnsaved() {
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
        int loadedShops = 0;
        for (File file : shopsFolder.listFiles()) {
            processShopFile(plugin, file);
            loadedShops++;
        }
        plugin.getLogger().info("Shops loaded: " + loadedShops);
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
        YamlShopFile shopFile = new YamlShopFile(plugin, ownerId);
        for (String shopName : shopFile.getPlayerShops()) {
            loadShop(plugin, file.getName(), shopFile, shopName);
        }
    }

    public static Shop loadShop(ChestShop plugin, String fileName, YamlShopFile shopFile, String shopName) {
        String shopPath = "shops." + shopName;
        String locationString = shopFile.getString(shopPath + ".location");
        if (locationString == null) {
            plugin.getLogger().warning("Invalid shop file: " + fileName + " (location is null) removing shop...");
            shopFile.removeShop(shopName);
            return null;
        }
        Location location = LocationUtils.toLocation(locationString);
        if (location.getWorld() == null || ShopUtils.isDisabledWorld(location.getWorld().getName())) {
            return null;
        }
        if (!location.getBlock().getType().equals(Material.CHEST)) {
            plugin.getLogger().warning("Invalid shop in: " + fileName + " (chest is not a chest) removing shop...");
            shopFile.removeShop(shopName);
            return null;
        }
        double sellPrice = shopFile.getDouble("shops." + shopName + ".sell-price", shopFile.getDouble("shops." + shopName + ".price", 0));
        double buyPrice = shopFile.getDouble("shops." + shopName + ".buy-price", 0);
        Material material = Material.getMaterial(shopFile.getString("shops." + shopName + ".material"));
        String createdAt = shopFile.getString("shops." + shopName + ".created");
        boolean isNotify = shopFile.getBoolean("shops." + shopName + ".do-notify");
        boolean canSell = shopFile.getBoolean("shops." + shopName + ".can-sell", true);
        boolean canBuy = shopFile.getBoolean("shops." + shopName + ".can-buy", false);
        return new Shop(shopFile.getOwnerId(), shopName, sellPrice, buyPrice, material, location, createdAt, isNotify, canSell, canBuy, shopFile.getAddedUuids(shopName));
    }

    public static YamlShopFile getShopFile(UUID ownerId) {
        return SHOPS_FILES.get(ownerId);
    }

    public static YamlShopFile getShopFile(Player owner) {
        return getShopFile(owner.getUniqueId());
    }

    public static void removeShopFile(UUID ownerId) {
        SHOPS_FILES.remove(ownerId);
    }

    public static void removeShopFile(Player owner) {
        removeShopFile(owner.getUniqueId());
    }

    public static Map<UUID, YamlShopFile> getShopFiles() {
        return new HashMap<>(SHOPS_FILES);
    }

    public static void saveShopFiles() {
        for (YamlShopFile shopFile : SHOPS_FILES.values()) {
            saveShopFile(shopFile);
        }
    }

    public static void saveShopFile(YamlShopFile shopFile) {
        if (shopFile.isSaved) {
            return;
        }
        shopFile.saveConfig();
        shopFile.isSaved = true;
    }

    public static String getPath(String shopName, String key) {
        return "shops." + shopName + "." + key;
    }

}
