package com.spygstudios.chestshop.database.yaml;

import java.io.File;
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
import org.bukkit.inventory.ItemStack;

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
    private ChestShop plugin;

    private static final Map<UUID, YamlShopFile> SHOPS_FILES = new HashMap<>();

    public YamlShopFile(ChestShop plugin, Player owner) {
        this(plugin, owner.getUniqueId());
    }

    public YamlShopFile(ChestShop plugin, UUID ownerId) {
        super("shops/" + ownerId + ".yml", plugin);
        if (SHOPS_FILES.containsKey(ownerId)) {
            return;
        }
        this.isSaved = true;
        this.ownerId = ownerId;
        this.plugin = plugin;
        setOrDefault("shops", null);
        setDefaultValues();
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

    private void setDefaultValues() {
        for (String shopName : getPlayerShops()) {
            String shopPath = "shops." + shopName;
            setOrDefault(shopPath + ".price", 0);
            setOrDefault(shopPath + ".sell-price", 0);
            setOrDefault(shopPath + ".buy-price", 0);
            setOrDefault(shopPath + ".do-notify", false);
            setOrDefault(shopPath + ".can-sell", true);
            setOrDefault(shopPath + ".can-buy", false);
            setOrDefault(shopPath + ".sold-items", 0);
            setOrDefault(shopPath + ".money-earned", 0);
            setOrDefault(shopPath + ".created", plugin.getDateString());
            isSaved = false;
        }
    }

    public void addShop(Shop shop) {
        setOrDefault(getPath(shop.getName(), ".price"), 0);
        setOrDefault(getPath(shop.getName(), ".item"), null);
        setOrDefault(getPath(shop.getName(), ".location"), LocationUtils.fromLocation(shop.getChestLocation(), true));
        setOrDefault(getPath(shop.getName(), ".do-notify"), false);
        setOrDefault(getPath(shop.getName(), ".created"), plugin.getDateString());
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

    public void setItem(String shopName, ItemStack item) {
        byte[] itemData = item != null ? item.serializeAsBytes() : null;
        set(getPath(shopName, ".item"), plugin.bytesToString(itemData));
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

    public void setCanBuy(String shopName, boolean canBuy) {
        set(getPath(shopName, ".can-buy"), canBuy);
        isSaved = false;
    }

    public void setCanSell(String shopName, boolean canSell) {
        set(getPath(shopName, ".can-sell"), canSell);
        isSaved = false;
    }

    public void markUnsaved() {
        isSaved = false;
    }

    public static void loadShopFiles(ChestShop plugin) {
        plugin.getLogger().info("Loading shops...");
        File shopsFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopsFolder.exists()) {
            shopsFolder.mkdirs();
            plugin.getLogger().info("Shops loaded!");
            return;
        }
        File[] files = shopsFolder.listFiles();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            int loadedShops = 0;
            for (File file : files) {
                loadedShops++;
                processShopFile(plugin, file);
            }
            plugin.getLogger().info("Shops loaded: " + loadedShops);
        });
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
        String itemData = shopFile.getString("shops." + shopName + ".item");
        ItemStack item = itemData != null ? ItemStack.deserializeBytes(plugin.stringToBytes(itemData)) : null;
        String createdAt = shopFile.getString("shops." + shopName + ".created");
        boolean isNotify = shopFile.getBoolean("shops." + shopName + ".do-notify");
        boolean canSell = shopFile.getBoolean("shops." + shopName + ".can-sell", true);
        boolean canBuy = shopFile.getBoolean("shops." + shopName + ".can-buy", false);
        return new Shop(shopFile.getOwnerId(), shopName, sellPrice, buyPrice, item, location, createdAt, isNotify, canSell, canBuy, shopFile.getAddedUuids(shopName));
    }

    public static YamlShopFile getShopFile(UUID ownerId) {
        YamlShopFile shopFile = SHOPS_FILES.get(ownerId);
        if (shopFile == null) {
            ChestShop plugin = ChestShop.getInstance();
            shopFile = new YamlShopFile(plugin, ownerId);
        }
        return shopFile;
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
