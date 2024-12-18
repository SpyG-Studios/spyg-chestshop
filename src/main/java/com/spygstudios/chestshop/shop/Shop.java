package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.color.TranslateColor;

public class Shop {

    private UUID owner;
    private double price;
    private int amount;
    private Material material;
    private Location chestLocation;
    private Location signLocation;
    private String name;

    private static final List<Shop> SHOPS = new ArrayList<Shop>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String name, Location chestLocation, Material material, int amount, double price) {
        this(owner.getUniqueId(), name, chestLocation, material, amount, price);
    }

    public Shop(UUID ownerId, String name, Location chestLocation, Material material, int amount, double price) {
        this.owner = ownerId;
        this.name = name;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.chestLocation = chestLocation;
        setShopSign();
        SHOPS.add(this);
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public Material getMaterial() {
        return material;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMaterial(Material material) {
        this.material = material;
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
        String materialName = material == null ? "-" : material.toString();
        for (int i = 0; i < 4; i++) {
            side.line(i, TranslateColor.translate(plugin.getConf().getString("shop.sign.line." + (i + 1)).replace("%owner%", Bukkit.getOfflinePlayer(owner).getName())
                    .replace("%amount%", String.valueOf(amount)).replace("%price%", String.valueOf(price)).replace("%material%", materialName)));
        }
        sign.update();
        this.signLocation = signLocation;
    }

    public void remove() {
        ShopFile.getShopFile(getOwner()).removeShop(getName());
        SHOPS.remove(this);
        signLocation.getBlock().setType(Material.AIR);
    }

    public static Shop getShop(String name) {
        for (Shop shop : SHOPS) {
            if (shop.getName().equalsIgnoreCase(name)) {
                return shop;
            }
        }
        return null;
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

    public static boolean isChestFaceFree(Block chestBlock) {
        BlockFace facing = getChestFace(chestBlock);
        Location signLocation = chestBlock.getLocation().clone().add(facing.getModX(), facing.getModY(), facing.getModZ());
        if (signLocation.getBlock().getType() == Material.AIR) {
            return true;
        }
        return false;
    }

}
