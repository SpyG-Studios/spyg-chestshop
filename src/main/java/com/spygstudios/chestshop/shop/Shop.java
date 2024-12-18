package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.color.TranslateColor;

import lombok.Getter;
import lombok.Setter;

public class Shop {

    @Getter
    private UUID owner;

    @Getter
    @Setter
    private double price;

    @Getter
    @Setter
    private int amount;

    @Getter
    @Setter
    private Material material;

    @Getter
    private Location chestLocation;

    @Getter
    private Location signLocation;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private boolean isNotify;

    private static final List<Shop> SHOPS = new ArrayList<Shop>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String name, Location chestLocation, Material material, int amount, double price, boolean doNotify) {
        this(owner.getUniqueId(), name, chestLocation, material, amount, price, doNotify);
    }

    public Shop(UUID ownerId, String name, Location chestLocation, Material material, int amount, double price, boolean doNotify) {
        this.owner = ownerId;
        this.name = name;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.chestLocation = chestLocation;
        this.isNotify = doNotify;
        setShopSign();
        SHOPS.add(this);
    }

    public double getPriceForEach() {
        return price / amount;
    }


    public String getMaterialString() {
        return material == null ? plugin.getConf().getString("shops.unknown-material") : material.toString();
    }


    public void setShopSign() {
        if (chestLocation.getBlock().getType() != Material.CHEST) {
            removeShop(this);
            throw new IllegalArgumentException("Block is not a chest, Shop removed! Location: " + chestLocation);
        }
        BlockFace chestFacing = getChestFace(chestLocation.getBlock());
        Location signLocation = chestLocation.clone().add(chestFacing.getModX(), chestFacing.getModY(), chestFacing.getModZ());
        Block signBlock = signLocation.getBlock();
        signBlock.setType(Material.OAK_WALL_SIGN);
        // TODO handle if sign is blocked by a block
        Sign sign = (Sign) signLocation.getBlock().getState();
        if (sign.getBlockData() instanceof Directional directional) {
            directional.setFacing(chestFacing);
            sign.setBlockData(directional);
        }
        SignSide side = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            side.line(i, TranslateColor.translate(plugin.getConf().getString("shop.sign.line." + (i + 1)).replace("%owner%", Bukkit.getOfflinePlayer(owner).getName())
                    .replace("%amount%", String.valueOf(amount)).replace("%price%", String.valueOf(price)).replace("%material%", getMaterialString())));
        }
        sign.update();
        this.signLocation = signLocation;
    }

    public void remove() {
        ShopFile.getShopFile(getOwner()).removeShop(getName());
        SHOPS.remove(this);
        signLocation.getBlock().setType(Material.AIR);
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
            if (shop.getOwner().equals(ownerId)) {
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
        ShopFile.getShopFile(shop.getOwner()).removeShop(shop.getName());
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
