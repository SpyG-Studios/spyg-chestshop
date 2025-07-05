package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.spyglib.hologram.HologramItemRow;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class Shop {
    @Getter
    private UUID ownerId;
    private double price;
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
    private double moneyEarned;
    @Getter
    private int soldItems;
    @Getter
    private ShopHologram hologram;
    private DataManager dataManager;
    @Getter
    @Setter
    private boolean isSaved = false;

    private static final List<Shop> SHOPS = new ArrayList<>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String shopName, Location chestLocation, String createdAt) {
        this(owner.getUniqueId(), shopName, 0, null, chestLocation, createdAt, false, new ArrayList<>());
    }

    public Shop(UUID owner, String shopName, Location chestLocation, String createdAt) {
        this(owner, shopName, 0, null, chestLocation, createdAt, false, new ArrayList<>());
    }

    public Shop(UUID ownerId, String shopName, double price, Material material, Location chestLocation, String createdAt, boolean isNotify, List<UUID> addedPlayers) {
        this.ownerId = ownerId;
        if (Shop.getShop(ownerId, shopName) != null) {
            return;
        }
        this.name = shopName;
        this.price = ShopUtils.parsePrice(price);
        this.material = material;
        this.chestLocation = chestLocation;
        this.createdAt = createdAt;
        this.isNotify = isNotify;
        this.addedPlayers = addedPlayers;
        this.dataManager = plugin.getDataManager();
        this.hologram = new ShopHologram(this, plugin);
        SHOPS.add(this);
    }

    public String getMaterialString() {
        if (material == null) {
            return plugin.getConf().getString("shops.unknown-material");
        }
        String materialString = material.toString();
        return materialString.length() > 14 ? materialString.substring(0, 14) : materialString;
    }

    public void setSoldItems(int soldItems) {
        dataManager.updateSoldItems(ownerId, name, soldItems).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name);
                return;
            }
            this.soldItems = soldItems;
            hologram.updateHologramRows();
            isSaved = false;
        });
    }

    public void setMoneyEarned(double moneyEarned) {
        dataManager.updateMoneyEarned(ownerId, name, moneyEarned).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name);
                return;
            }
            this.moneyEarned = moneyEarned;
            hologram.updateHologramRows();
            isSaved = false;
        });
    }

    public String getChestLocationString() {
        return chestLocation.getWorld().getName() + ", x: " + chestLocation.getBlockX() + " y: " + chestLocation.getBlockY() + " z: " + chestLocation.getBlockZ();
    }

    public void setMaterial(Material material) {
        dataManager.updateShopMaterial(ownerId, name, material).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop material for " + name);
                return;
            }
            this.material = material;
            if (hologram.getHologram().getRows().get(plugin.getConf().getStringList("shops.lines").size()) instanceof HologramItemRow row) {
                row.setItem(new ItemStack(material));
            }
            isSaved = false;
        });
    }

    public void setName(String newName) {
        dataManager.renameShop(ownerId, name, newName).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to rename shop " + name + " to " + newName);
                return;
            }
            this.name = newName;
            hologram.updateHologramRows();
            isSaved = false;
        });
    }

    public double getPrice() {
        return ShopUtils.parsePrice(this.price);
    }

    public void setPrice(double price) {
        double parsedPrice = ShopUtils.parsePrice(price);
        dataManager.updateShopPrice(ownerId, name, parsedPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop price for " + name);
                return;
            }
            this.price = parsedPrice;
            hologram.updateHologramRows();
            isSaved = false;
        });
    }

    public void setNotify(boolean notify) {
        dataManager.updateShopNotify(ownerId, name, notify).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop notify setting for " + name);
                return;
            }
            isNotify = notify;
            isSaved = false;
        });
    }

    public void addPlayer(OfflinePlayer player) {
        addPlayer(player.getUniqueId());
    }

    public void addPlayer(UUID uuid) {
        if (addedPlayers.contains(uuid)) {
            Message.PLAYER_ALREADY_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        dataManager.addPlayerToShop(ownerId, name, uuid).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to add player " + uuid + " to shop " + name);
                return;
            }
            addedPlayers.add(uuid);
            Message.PLAYER_ADDED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            isSaved = false;
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
        dataManager.removePlayerFromShop(ownerId, name, uuid).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to remove player " + uuid + " from shop " + name);
                return;
            }
            addedPlayers.remove(uuid);
            Message.PLAYER_REMOVED.send(Bukkit.getPlayer(ownerId), Map.of("%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            isSaved = false;
        });
    }

    public void remove(Player remover, ShopRemoveCause cause) {
        ShopRemoveEvent shopRemoveEvent = new ShopRemoveEvent(this, cause, remover);
        Bukkit.getPluginManager().callEvent(shopRemoveEvent);
        if (shopRemoveEvent.isCancelled()) {
            return;
        }
        dataManager.deleteShop(ownerId, name).thenAccept(success -> {
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
        return InventoryUtils.countItems(chest.getInventory(), material);
    }

    public void openShopInventory(Player player) {
        if (!(chestLocation.getBlock().getState() instanceof Chest chest)) {
            return;
        }
        player.openInventory(chest.getInventory());
    }

    public void sell(Player buyer, int amount) {
        int itemsLeft = getItemsLeft();
        int itemCount = itemsLeft < amount ? itemsLeft : amount;
        if (!InventoryUtils.hasFreeSlot(buyer)) {
            Message.SHOP_INVENTORY_FULL.send(buyer);
            return;
        }

        double itemsPrice = itemCount * price;
        Economy economy = plugin.getEconomy();
        EconomyResponse response = economy.withdrawPlayer(buyer, itemsPrice);

        if (!response.transactionSuccess()) {
            Message.NOT_ENOUGH_MONEY.send(buyer, Map.of("%price%", String.valueOf(itemsPrice)));
            return;
        }

        economy.depositPlayer(Bukkit.getOfflinePlayer(getOwnerId()), itemsPrice);
        extractItems(buyer, (Chest) getChestLocation().getBlock().getState(), itemCount);
        itemsLeft = itemsLeft - itemCount;

        Message.SHOP_BOUGHT.send(buyer, Map.of(
                "%price%", String.valueOf(itemsPrice),
                "%material%", material.name(),
                "%items-left%", String.valueOf(itemsLeft),
                "%items-bought%", String.valueOf(itemCount)));

        dataManager.updateShopStats(ownerId, name, itemCount, itemsPrice).thenAccept(success -> {
            if (!success) {
                plugin.getLogger().warning("Failed to update shop stats for " + name + " owned by " + ownerId);
                return;
            }
            setSoldItems(getSoldItems() + itemCount);
            setMoneyEarned(getMoneyEarned() + itemsPrice);
            isSaved = false;
        });

        Player owner = Bukkit.getPlayer(ownerId);
        if (isNotify && owner != null) {
            Message.SHOP_SOLD.send(owner, Map.of(
                    "%price%", String.valueOf(itemsPrice),
                    "%material%", material.name(),
                    "%player-name%", buyer.getName(),
                    "%items-left%", String.valueOf(itemsLeft),
                    "%items-bought%", String.valueOf(itemCount)));
        }
    }

    public void buy(Player seller, int amount) {
        // Implement the buy logic if needed
        // This method is currently not used in the provided code
    }

    private int extractItems(Player buyer, Chest chest, int itemCount) {
        for (ItemStack chestItem : chest.getInventory().getContents()) {
            if (itemCount <= 0)
                break;

            if (chestItem != null && chestItem.getType() == material) {
                int chestAmount = chestItem.getAmount();
                int removeAmount = Math.min(itemCount, chestAmount);

                ItemStack clone = chestItem.clone();
                clone.setAmount(removeAmount);

                HashMap<Integer, ItemStack> leftover = buyer.getInventory().addItem(clone);

                if (leftover.isEmpty()) {
                    chestItem.setAmount(chestAmount - removeAmount);
                    itemCount -= removeAmount;
                } else {
                    int added = removeAmount - leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    chestItem.setAmount(chestAmount - added);
                    itemCount -= added;
                    break;
                }
            }
        }

        return itemCount;
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
