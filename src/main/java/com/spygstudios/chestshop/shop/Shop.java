package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.ShopRemoveCause;
import com.spygstudios.chestshop.events.ShopRemoveEvent;
import com.spygstudios.spyglib.hologram.HologramItemRow;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import lombok.Getter;

public class Shop {
    @Getter
    private UUID ownerId;
    @Getter
    private int price;
    @Getter
    private Material material;
    @Getter
    private Location chestLocation;
    @Getter
    private String createdAt;
    @Getter
    private String name;
    @Getter
    private boolean isNotify;
    @Getter
    private List<UUID> addedPlayers;
    @Getter
    private ShopTransactions shopTransactions;
    @Getter
    private ShopHologram hologram;

    private ShopFile shopFile;

    private static final List<Shop> SHOPS = new ArrayList<>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String shopName, Location chestLocation, ShopFile shopFile) {
        this(owner.getUniqueId(), shopName, 0, null, chestLocation, ShopFile.getDateString(), false, new ArrayList<>(), shopFile);
        shopFile.addShop(this);
    }

    public Shop(UUID ownerId, String shopName, int price, Material material, Location chestLocation, String createdAt, boolean isNotify, List<UUID> addedPlayers, ShopFile shopFile) {
        this.ownerId = ownerId;
        this.name = shopName;
        this.price = price;
        this.material = material;
        this.chestLocation = chestLocation;
        this.createdAt = createdAt;
        this.isNotify = isNotify;
        this.addedPlayers = addedPlayers;
        this.shopFile = shopFile;
        this.shopTransactions = new ShopTransactions(this, shopFile);
        this.hologram = new ShopHologram(this, plugin);

        SHOPS.add(this);
    }

    public String getMaterialString() {
        if (material == null) {
            return plugin.getConf().getString("shops.unknown-material");
        }
        String materialString = getMaterial().toString();
        return materialString.length() > 14 ? materialString.substring(0, 14) : materialString;
    }

    public int getSoldItems() {
        return shopFile.getInt("shops." + name + ".sold-items");
    }

    public int getMoneyEarned() {
        return shopFile.getInt("shops." + name + ".money-earned");
    }

    public String getChestLocationString() {
        return chestLocation.getWorld().getName() + ", x: " + chestLocation.getBlockX() + " y: " + chestLocation.getBlockY() + " z: " + chestLocation.getBlockZ();
    }

    public void setMaterial(Material material) {
        this.material = material;
        ShopFile.getShopFile(ownerId).setMaterial(name, material);
        if (hologram.getHologram().getRows().get(plugin.getConf().getStringList("shop.lines").size()) instanceof HologramItemRow row) {
            row.setItem(new ItemStack(material));
        }
    }

    public void setName(String newName) {
        ShopFile.getShopFile(ownerId).setName(name, newName);
        this.name = newName;
        hologram.updateHologramRows();
    }

    public void setPrice(int price) {
        this.price = price;
        ShopFile.getShopFile(ownerId).setPrice(name, price);
        hologram.updateHologramRows();
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
        shopFile.overwriteSet("shops." + name + ".do-notify", notify);
        shopFile.save();
    }

    public void addPlayer(OfflinePlayer player) {
        addPlayer(player.getUniqueId());
    }

    public void addPlayer(UUID uuid) {
        if (addedPlayers.contains(uuid)) {
            Message.PLAYER_ALREADY_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        addedPlayers.add(uuid);
        shopFile.addPlayer(uuid, name);
        Message.PLAYER_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
    }

    public void removePlayer(OfflinePlayer player) {
        removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        if (!addedPlayers.contains(uuid)) {
            Message.PLAYER_NOT_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        addedPlayers.remove(uuid);
        shopFile.removePlayer(uuid, name);
        Message.PLAYER_REMOVED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
    }

    public void remove(Player remover, ShopRemoveCause cause) {
        ShopRemoveEvent shopRemoveEvent = new ShopRemoveEvent(this, cause, remover);
        Bukkit.getPluginManager().callEvent(shopRemoveEvent);
        Shop.removeShop(this);
    }

    public int getItemsLeft() {
        Chest chest = (Chest) chestLocation.getBlock().getState();
        return InventoryUtils.countItems(chest.getInventory(), material);
    }

    public void openShopInventory(Player player) {
        if (!(chestLocation.getBlock().getState() instanceof Chest chest)) {
            return;
        }
        player.openInventory(chest.getInventory());
    }

    public static List<Shop> getShops(Player owner) {
        return getShops(owner.getUniqueId());
    }

    public static List<Shop> getShops(UUID ownerId) {
        List<Shop> shops = new ArrayList<>();
        for (Shop shop : SHOPS) {
            if (shop.getOwnerId().equals(ownerId)) {
                shops.add(shop);
            }
        }
        return shops;
    }

    public static List<Shop> getShops() {
        return new ArrayList<>(SHOPS);
    }

    public static Shop getShop(UUID ownerId, String name) {
        for (Shop shop : SHOPS) {
            if (shop.getOwnerId().equals(ownerId) && shop.getName().equalsIgnoreCase(name)) {
                return shop;
            }
        }
        return null;
    }

    public static Shop getShop(Location location) {
        for (Shop shop : SHOPS) {
            try {
                Block shopBlock = shop.getChestLocation().getBlock();
                if (shop.getChestLocation().equals(location) || (ShopUtils.isDoubleChest(shopBlock) && ShopUtils.getAdjacentChest(shopBlock).getLocation().equals(location))) {
                    return shop;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static void removeShop(Shop shop) {
        ShopFile.getShopFile(shop.getOwnerId()).removeShop(shop.getName());
        shop.hologram.removeHologram();
        SHOPS.remove(shop);
    }

}
