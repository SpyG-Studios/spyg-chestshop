package hu.spyg.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

import hu.spyg.chestshop.ChestShop;
import hu.spyg.spyglib.color.TranslateColor;

public class Shop {

    private UUID owner;
    private double price;
    private int amount;
    private Material material;
    private Location location;
    private String name;

    private static final List<Shop> SHOPS = new ArrayList<Shop>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String name, Chest chest) {
        this.owner = owner.getUniqueId();
        this.name = name;
        this.location = chest.getLocation();
        BlockFace chestFacing = getChestFace(chest.getBlock());
        Location signLocation = chest.getLocation().clone().add(chestFacing.getModX(), chestFacing.getModY(), chestFacing.getModZ());
        signLocation.getBlock().setType(Material.OAK_WALL_SIGN);
        Sign signBlock = (Sign) signLocation.getBlock().getState();
        if (signBlock.getBlockData() instanceof Directional directional) {
            directional.setFacing(chestFacing);
            signBlock.setBlockData(directional);
        }

        SignSide side = signBlock.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            side.line(i, TranslateColor.translate(
                    plugin.getConf().getString("shop.sign.line." + (i + 1)).replace("%owner%", owner.getName()).replace("%amount%", String.valueOf(amount)).replace("%price%", String.valueOf(price))));
        }
        signBlock.update();
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

    public Location getLocation() {
        return location;
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

    public static List<Shop> getShops() {
        return new ArrayList<Shop>(SHOPS);
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
