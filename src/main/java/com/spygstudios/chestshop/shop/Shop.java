package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.inventory.InventoryUtils;
import com.spygstudios.spyglib.location.LocationUtils;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.EconomyResponse;

public class Shop {

    @Getter
    private UUID ownerId;

    @Getter
    @Setter
    private double price;

    @Getter
    @Setter
    private int amount;

    @Getter
    private Material material;

    @Getter
    private Location chestLocation;

    @Getter
    private Location signLocation;

    @Getter
    private String createdAt;

    @Getter
    @Setter
    private String name;

    @Getter
    private boolean isNotify;

    private ShopFile shopFile;

    private static final List<Shop> SHOPS = new ArrayList<Shop>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String name, ShopFile shopFile) {
        this(owner.getUniqueId(), name, shopFile);
    }

    public Shop(UUID ownerId, String name, ShopFile shopFile) {
        this.ownerId = ownerId;
        this.name = name;
        this.shopFile = shopFile;
        price = shopFile.getDouble("shops." + name + ".price");
        amount = shopFile.getInt("shops." + name + ".amount");
        material = Material.getMaterial(shopFile.getString("shops." + name + ".material"));
        chestLocation = LocationUtils.toLocation(shopFile.getString("shops." + name + ".location"));
        createdAt = shopFile.getString("shops." + name + ".created");
        isNotify = shopFile.getBoolean("shops." + name + ".do-notify");

        setShopSign();
    }

    public double getPriceForEach() {
        return price / amount;
    }

    public String getMaterialString() {
        return material == null ? plugin.getConf().getString("shops.unknown-material") : material.toString();
    }

    public int getSoldItems() {
        return shopFile.getInt("shops." + name + ".sold-items");
    }

    public double getMoneyEarned() {
        return shopFile.getDouble("shops." + name + ".money-earned");
    }

    public String getChestLocationString() {
        return chestLocation.getWorld().getName() + ", x: " + chestLocation.getBlockX() + " y: " + chestLocation.getBlockY() + " z: " + chestLocation.getBlockZ();
    }

    public void setMaterial(Material material) {
        this.material = material;
        setShopSign();
        ShopFile.getShopFile(ownerId).setMaterial(name, material);
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
        shopFile.overwriteSet("shops." + name + ".do-notify", notify);
        shopFile.save();
    }

    public void setShopSign() {
        if (chestLocation.getBlock().getType() != Material.CHEST) {
            removeShop(this);
            throw new IllegalArgumentException("Block is not a chest, Shop removed! Location: " + chestLocation);
        }
        BlockFace chestFacing = getChestFace(chestLocation.getBlock());
        Location signLocation = chestLocation.clone().add(chestFacing.getModX(), chestFacing.getModY(), chestFacing.getModZ());
        Block signBlock = signLocation.getBlock();
        if (!(signBlock.getBlockData() instanceof WallSign) && signBlock.getType() != Material.AIR) {
            Bukkit.getLogger().warning("Shop sign is not a sign or air, removing shop! " + getName() + " at " + getChestLocationString());
            remove();
            return;
        }

        Sign sign = (Sign) signLocation.getBlock().getState();
        SignSide side = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            side.line(i, TranslateColor.translate(plugin.getConf().getString("shop.sign.line." + (i + 1)).replace("%owner%", Bukkit.getOfflinePlayer(ownerId).getName())
                    .replace("%amount%", String.valueOf(amount)).replace("%price%", String.valueOf(price)).replace("%material%", getMaterialString())));
        }
        sign.update();
        this.signLocation = signLocation;
        SHOPS.add(this);
    }

    public void remove() {
        Shop.removeShop(this);
        if (signLocation == null) {
            return;
        }
        signLocation.getBlock().setType(Material.AIR);
    }

    public void sell(Player buyer) {
        if (getMaterial() == null || getAmount() == 0) {
            Message.SHOP_SETUP_NEEDED.sendMessage(buyer);
            return;
        }
        Chest chest = (Chest) getChestLocation().getBlock().getState();

        int itemCount, itemsLeft;
        itemCount = itemsLeft = InventoryUtils.countItems(chest.getInventory(), getMaterial());
        if (itemCount == 0) {
            Message.SHOP_EMPTY.sendMessage(buyer);
            return;
        }
        itemCount = getAmount() > itemCount ? itemCount : getAmount();
        itemsLeft -= itemCount;
        double price = getPriceForEach() * itemCount;
        EconomyResponse response = plugin.getEconomy().withdrawPlayer(buyer, getPrice());
        if (response.transactionSuccess()) {
            ChestShop.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getOwnerId()), price);
            buyer.getInventory().addItem(new ItemStack(getMaterial(), itemCount));
            chest.getInventory().removeItem(new ItemStack(getMaterial(), itemCount));
            Message.SHOP_BOUGHT.sendMessage(buyer, Map.of("%price%", String.valueOf(price), "%material%", getMaterial().name(), "%items-left%", String.valueOf(itemsLeft)));
            shopFile.overwriteSet("shops." + getName() + ".sold-items", shopFile.getInt("shops." + getName() + ".sold-items") + itemCount);
            shopFile.overwriteSet("shops." + getName() + ".money-earned", shopFile.getDouble("shops." + getName() + ".money-earned") + price);
            shopFile.save();
            Player owner = Bukkit.getPlayer(getOwnerId());
            if (isNotify() && owner != null) {
                Message.SHOP_SOLD.sendMessage(owner,
                        Map.of("%price%", String.valueOf(price), "%material%", getMaterial().name(), "%player-name%", buyer.getName(), "%items-left%", String.valueOf(itemsLeft)));
            }
            return;
        }
        Message.NOT_ENOUGH_MONEY.sendMessage(buyer, Map.of("%price%", String.valueOf(price)));
    }

    public static boolean isDisabledWorld(String worldName) {
        return Bukkit.getWorld(worldName) != null && isDisabledWorld(Bukkit.getWorld(worldName));
    }

    public static boolean isDisabledWorld(World world) {
        for (String worldName : plugin.getConf().getStringList("shops.disabled-worlds")) {
            if (world.getName().equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    public static Shop getShop(String name) {
        for (Shop shop : SHOPS) {
            if (shop.getName().equalsIgnoreCase(name)) {
                return shop;
            }
        }
        return null;
    }

    public static List<Shop> getShops(Player owner) {
        return getShops(owner.getUniqueId());
    }

    public static List<Shop> getShops(UUID ownerId) {
        List<Shop> shops = new ArrayList<Shop>();
        for (Shop shop : SHOPS) {
            if (shop.getOwnerId().equals(ownerId)) {
                shops.add(shop);
            }
        }
        return shops;
    }

    public static Shop getShop(Location location) {
        for (Shop shop : SHOPS) {
            if (shop.getChestLocation().equals(location) || shop.getSignLocation().equals(location)) {
                return shop;
            }
        }
        return null;
    }

    public static List<Shop> getShops() {
        return new ArrayList<Shop>(SHOPS);
    }

    public static void removeShop(Shop shop) {
        ShopFile.getShopFile(shop.getOwnerId()).removeShop(shop.getName());
        SHOPS.remove(shop);
    }

    public static BlockFace getChestFace(Block chestBlock) {
        if (!(chestBlock.getBlockData() instanceof Directional directional)) {
            throw new IllegalArgumentException("Block is not a chest!");
        }
        return directional.getFacing();
    }

    public static boolean isDoubleChest(Block block) {
        if (!(block.getState() instanceof Chest chest)) {
            return false;
        }
        Inventory inv = chest.getInventory();
        if (inv instanceof DoubleChestInventory) {
            return true;
        }
        return false;
    }

    public static Block getAdjacentChest(Block block) {
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
            Block relativeBlock = block.getRelative(face);
            if (relativeBlock.getState() instanceof Chest) {
                return relativeBlock;
            }
        }
        return null;
    }

    public static boolean isChestFaceFree(Block chestBlock) {
        BlockFace facing = getChestFace(chestBlock);
        Location signLocation = chestBlock.getLocation().clone().add(facing.getModX(), facing.getModY(), facing.getModZ());
        if (signLocation.getBlock().getType() == Material.AIR) {
            return true;
        }
        return false;
    }

}
