package com.spygstudios.chestshop.shop.yaml;

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
import com.spygstudios.chestshop.interfaces.ShopFile;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.spyglib.location.LocationUtils;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

import lombok.Getter;

public class ShopYmlFile extends YamlManager implements ShopFile {
    @Getter
    private UUID ownerId;
    private boolean isSaved;
    private static final Map<UUID, ShopYmlFile> SHOPS_FILES = new HashMap<>();

    public ShopYmlFile(ChestShop plugin, Player owner) {
        this(plugin, owner.getUniqueId());
    }

    public ShopYmlFile(ChestShop plugin, UUID ownerId) {
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
        overwriteSet(getPath(shopName, ".added-players"), players.stream().map(UUID::toString).toList());
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
        if (getPlayerShops().contains(shopName)) {
            overwriteSet("shops." + shopName, null);
            isSaved = false;
        }
    }

    private static void setDefaultValues(ShopYmlFile shopFile) {
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
        set(getPath(shop.getName(), ".price"), 0);
        set(getPath(shop.getName(), ".material"), null);
        set(getPath(shop.getName(), ".location"), LocationUtils.fromLocation(shop.getChestLocation(), true));
        set(getPath(shop.getName(), ".do-notify"), false);
        set(getPath(shop.getName(), ".created"), getDateString());
        set(getPath(shop.getName(), ".added-players"), new ArrayList<String>());
        isSaved = false;
    }

    public void renameShop(String oldName, String newName) {
        ConfigurationSection section = getConfigurationSection("shops." + oldName);
        if (section != null) {
            set("shops." + newName, section);
            overwriteSet("shops." + oldName, null);
            isSaved = false;
        }
    }

    public void setMaterial(String shopName, Material material) {
        String matName = material != null ? material.name() : null;
        overwriteSet(getPath(shopName, ".material"), matName);
        isSaved = false;
    }

    public void setPrice(String shopName, double price) {
        overwriteSet(getPath(shopName, ".price"), price);
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
        ShopYmlFile shopFile = new ShopYmlFile(plugin, ownerId);
        for (String shopName : shopFile.getPlayerShops()) {
            processShop(plugin, file, shopFile, shopName);
        }
    }

    private static void processShop(ChestShop plugin, File file, ShopYmlFile shopFile, String shopName) {
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

    public static ShopYmlFile getShopFile(UUID ownerId) {
        return SHOPS_FILES.get(ownerId);
    }

    public static ShopYmlFile getShopFile(Player owner) {
        return getShopFile(owner.getUniqueId());
    }

    public static void removeShopFile(UUID ownerId) {
        SHOPS_FILES.remove(ownerId);
    }

    public static void removeShopFile(Player owner) {
        removeShopFile(owner.getUniqueId());
    }

    public static Map<UUID, ShopYmlFile> getShopFiles() {
        return new HashMap<>(SHOPS_FILES);
    }

    public static void startSaveScheduler(ChestShop plugin) {
        long interval = plugin.getConf().getInt("shops.save-interval", 60);
        if (interval <= 0)
            interval = 60;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, ShopYmlFile::saveShops, 0, 20L * interval);
    }

    public static void saveShops() {
        for (ShopYmlFile shopFile : SHOPS_FILES.values()) {
            if (shopFile.isSaved) {
                continue;
            }
            shopFile.saveConfig();
            shopFile.isSaved = true;
        }
    }

    private String getPath(String shopName, String key) {
        return "shops." + shopName + "." + key;
    }

}
