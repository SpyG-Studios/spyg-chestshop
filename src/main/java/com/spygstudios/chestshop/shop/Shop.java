package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.ShopRemoveCause;
import com.spygstudios.chestshop.events.ShopRemoveEvent;
import com.spygstudios.spyglib.hologram.HologramItemRow;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

@Getter
public class Shop {
    private UUID ownerId;
    private double sellPrice;
    private double buyPrice;
    private ItemStack item;
    private Location chestLocation;
    private String createdAt;
    private String name;
    private boolean isNotify;
    private boolean canSellToPlayers;
    private boolean canBuyFromPlayers;
    private List<UUID> addedPlayers;
    private double moneyEarned;
    private double moneySpent;
    private int soldItems;
    private int boughtItems;
    private ShopHologram hologram;
    @Setter
    private boolean isSaved = false;
    private ShopTransactions shopTransactions;

    private static final List<Shop> SHOPS = new ArrayList<>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String shopName, Location chestLocation, String createdAt) {
        this(owner.getUniqueId(), shopName, 0, 0, null, chestLocation, createdAt, false, true, false, new ArrayList<>());
    }

    public Shop(UUID ownerId, String shopName, Location chestLocation, String createdAt) {
        this(ownerId, shopName, 0, 0, null, chestLocation, createdAt, false, true, false, new ArrayList<>());
    }

    public Shop(UUID ownerId, String shopName, double sellPrice, double buyPrice, ItemStack item, Location chestLocation, String createdAt, boolean isNotify, boolean canSell, boolean canBuy,
            List<UUID> addedPlayers) {
        synchronized (SHOPS) {
            if (Shop.getShop(ownerId, shopName) != null) {
                return;
            }
            SHOPS.add(this);
        }
        this.ownerId = ownerId;
        this.name = shopName;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.item = item;
        this.chestLocation = chestLocation;
        this.createdAt = createdAt;
        this.isNotify = isNotify;
        this.canSellToPlayers = canSell;
        this.canBuyFromPlayers = canBuy;
        this.addedPlayers = addedPlayers;
        this.shopTransactions = new ShopTransactions(this);
        this.hologram = new ShopHologram(this, plugin);
    }

    public String getItemName() {
        if (item == null) {
            return plugin.getConf().getString("shops.unknown.item");
        }
        Component displayName = item.getItemMeta().displayName();
        if (displayName != null && !displayName.equals(Component.empty())) {
            return ((TextComponent) displayName).content();
        }
        return item.getType().name();
    }

    public ItemStack getItem() {
        if (item == null) {
            return null;
        }
        return item.clone();
    }

    public double getSellPrice() {
        return ShopUtils.parsePrice(this.sellPrice);
    }

    public double getBuyPrice() {
        return ShopUtils.parsePrice(this.buyPrice);
    }

    public boolean acceptsCustomerPurchases() {
        return isCanSellToPlayers();
    }

    public boolean acceptsCustomerSales() {
        return isCanBuyFromPlayers();
    }

    public double getCustomerPurchasePrice() {
        return getSellPrice();
    }

    public double getCustomerSalePrice() {
        return getBuyPrice();
    }

    public void setSoldItems(int soldItems) {
        plugin.getDataManager().updateSoldItems(ownerId, name, soldItems).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name);
                return;
            }
            this.soldItems = soldItems;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setMoneyEarned(double moneyEarned) {
        plugin.getDataManager().updateMoneyEarned(ownerId, name, moneyEarned).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name);
                return;
            }
            this.moneyEarned = moneyEarned;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setBoughtItems(int boughtItems) {
        plugin.getDataManager().updateSoldItems(ownerId, name, boughtItems).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name);
                return;
            }
            this.soldItems = boughtItems;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setMoneySpent(double moneySpent) {
        plugin.getDataManager().updateMoneyEarned(ownerId, name, moneySpent).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name);
                return;
            }
            this.moneyEarned = moneySpent;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public String getChestLocationString() {
        return chestLocation.getWorld().getName() + ", x: " + chestLocation.getBlockX() + " y: " + chestLocation.getBlockY() + " z: " + chestLocation.getBlockZ();
    }

    public void setShopItem(ItemStack item) {
        plugin.getDataManager().updateShopItem(ownerId, name, item).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop item for " + name);
                return;
            }
            this.item = item.clone();
            this.item.setAmount(1);
            ItemMeta meta = this.item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                damageable.setDamage(0);
                this.item.setItemMeta(meta);
            }
            if (hologram.getHologram().getRows().get(plugin.getConf().getStringList("shops.lines").size()) instanceof HologramItemRow row) {
                row.setItem(this.item);
            }
            this.isSaved = false;
        });
    }

    public void setName(String newName) {
        plugin.getDataManager().renameShop(ownerId, name, newName).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to rename shop " + name + " to " + newName);
                return;
            }
            this.name = newName;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setBuyPrice(double buyPrice) {
        double parsedPrice = ShopUtils.parsePrice(buyPrice);
        plugin.getDataManager().updateShopBuyPrice(ownerId, name, parsedPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop price for " + name);
                return;
            }
            this.buyPrice = parsedPrice;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setSellPrice(double sellPrice) {
        double parsedPrice = ShopUtils.parsePrice(sellPrice);
        plugin.getDataManager().updateShopSellPrice(ownerId, name, parsedPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop price for " + name);
                return;
            }
            this.sellPrice = parsedPrice;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setNotify(boolean notify) {
        plugin.getDataManager().updateShopNotify(ownerId, name, notify).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop notify setting for " + name);
                return;
            }
            this.isNotify = notify;
            this.isSaved = false;
        });
    }

    public void setCanSellToPlayers(boolean canSellToPlayers) {
        plugin.getDataManager().setCanSellToPlayers(ownerId, name, canSellToPlayers).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop canSellToPlayers setting for " + name);
                return;
            }
            this.canSellToPlayers = canSellToPlayers;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void setCanBuyFromPlayers(boolean canBuyFromPlayers) {
        plugin.getDataManager().setCanBuyFromPlayers(ownerId, name, canBuyFromPlayers).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop canBuyFromPlayers setting for " + name);
                return;
            }
            this.canBuyFromPlayers = canBuyFromPlayers;
            this.hologram.updateHologramRows();
            this.isSaved = false;
        });
    }

    public void addPlayer(UUID uuid) {
        if (addedPlayers.contains(uuid)) {
            Message.PLAYER_ALREADY_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        plugin.getDataManager().addPlayerToShop(ownerId, name, uuid).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to add player " + uuid + " to shop " + name);
                return;
            }
            this.addedPlayers.add(uuid);
            Message.PLAYER_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            this.isSaved = false;
        });
    }

    public void removePlayer(OfflinePlayer player) {
        removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        if (!addedPlayers.contains(uuid)) {
            Message.PLAYER_NOT_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        plugin.getDataManager().removePlayerFromShop(ownerId, name, uuid).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to remove player " + uuid + " from shop " + name);
                return;
            }
            this.addedPlayers.remove(uuid);
            Message.PLAYER_REMOVED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            this.isSaved = false;
        });
    }

    public void remove(Player remover, ShopRemoveCause cause) {
        ShopRemoveEvent shopRemoveEvent = new ShopRemoveEvent(this, cause, remover);
        Bukkit.getPluginManager().callEvent(shopRemoveEvent);
        if (shopRemoveEvent.isCancelled()) {
            return;
        }
        plugin.getDataManager().deleteShop(ownerId, name).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to remove shop " + name);
                return;
            }
            hologram.removeHologram();
            SHOPS.remove(this);
        });
    }

    public int getItemsLeft() {
        Chest chest = (Chest) chestLocation.getBlock().getState();
        return ShopUtils.countDurableItemsInInventory(chest.getInventory(), item);

    }

    public void openShopInventory(Player player) {
        if (!(chestLocation.getBlock().getState() instanceof Chest chest)) {
            return;
        }
        player.openInventory(chest.getInventory());
    }

    public void unload() {
        if (hologram != null) {
            hologram.removeHologram();
        }
        SHOPS.remove(this);
    }

    public static List<Shop> getShops(Player owner) {
        return getShops(owner.getUniqueId());
    }

    public static List<Shop> getShops(UUID ownerId) {
        List<Shop> shops = new ArrayList<>();
        for (Shop shop : getShops()) {
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
        for (Shop shop : getShops()) {
            if (shop.getOwnerId().equals(ownerId) && shop.getName().equalsIgnoreCase(name)) {
                return shop;
            }
        }
        return null;
    }

    public static Shop getShop(Location location) {
        for (Shop shop : getShops()) {
            Location shopLoc = shop.getChestLocation();
            if (!shopLoc.getWorld().isChunkLoaded(shopLoc.getBlockX() >> 4, shopLoc.getBlockZ() >> 4))
                continue;
            Block shopBlock = shopLoc.getBlock();
            if (shopLoc.equals(location)) {
                return shop;
            }
            if (ShopUtils.isDoubleChest(shopBlock)) {
                Block adj = ShopUtils.getAdjacentChest(shopBlock);
                if (adj != null && adj.getLocation().equals(location)) {
                    return shop;
                }
            }
        }
        return null;
    }

}
