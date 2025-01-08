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
        SHOPS_FILES.put(ownerId, this);
        isSaved = true;
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
            return new HashSet<String>();
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

    public void addShop(Player owner, String name, Location chestLocation) {
        set("shops." + name + ".price", 0);
        set("shops." + name + ".amount", 0);
        set("shops." + name + ".material", null);
        set("shops." + name + ".location", LocationUtils.fromLocation(chestLocation, true));
        set("shops." + name + ".do-notify", false);
        set("shops." + name + ".created", getDateString());
        set("shops." + name + ".added-players", new ArrayList<String>());
        new Shop(owner.getUniqueId(), name, this);
        isSaved = false;
    }

    public void setMaterial(String shopName, Material material) {
        overwriteSet("shops." + shopName + ".material", material == null ? null : material.name());
        isSaved = false;
    }

    public void setPrice(String shopName, int price) {
        overwriteSet("shops." + shopName + ".price", price);
        isSaved = false;
    }

    public void setAmount(String shopName, int amount) {
        overwriteSet("shops." + shopName + ".amount", amount);
        isSaved = false;
    }

    public void save() {
        isSaved = false;
    }

    private static void setDefaultValues(ShopFile shopFile) {
        for (String shopName : shopFile.getPlayerShops()) {
            String shopPath = "shops." + shopName;
            shopFile.set(shopPath + ".price", 0);
            shopFile.set(shopPath + ".amount", 0);
            shopFile.set(shopPath + ".do-notify", false);
            shopFile.set(shopPath + ".sold-items", 0);
            shopFile.set(shopPath + ".money-earned", 0);
            shopFile.set(shopPath + ".created", getDateString());
            shopFile.isSaved = false;
        }
    }

    private static String getDateString() {
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
            if (file.isFile() && file.getName().endsWith(".yml")) {
                try {
                    UUID ownerId = UUID.fromString(file.getName().replace(".yml", ""));
                    ShopFile shopFile = new ShopFile(plugin, ownerId);
                    setDefaultValues(shopFile);
                    for (String shopName : shopFile.getPlayerShops()) {
                        String shopPath = "shops." + shopName;
                        String locationString = shopFile.getString(shopPath + ".location");
                        if (locationString == null) {
                            plugin.getLogger().warning("Invalid shop file: " + file.getName() + " (location is null) removing shop...");
                            shopFile.removeShop(shopName);
                            continue;
                        }
                        Location location = LocationUtils.toLocation(locationString);
                        if (Shop.isDisabledWorld(location.getWorld())) {
                            continue;
                        }
                        new Shop(ownerId, shopName, shopFile);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid shop file: " + file.getName() + " (invalid UUID)");
                }
            }
        }
        plugin.getLogger().info("Shops loaded!");
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
        for (ShopFile shopFile : SHOPS_FILES.values()) {
            if (shopFile.isSaved) {
                continue;
            }
            shopFile.saveConfig();
            shopFile.isSaved = true;
        }
    }
}
