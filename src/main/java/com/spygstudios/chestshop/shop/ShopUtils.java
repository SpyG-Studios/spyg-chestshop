package com.spygstudios.chestshop.shop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import com.spygstudios.chestshop.ChestShop;

public class ShopUtils {

    private static ChestShop plugin = ChestShop.getInstance();

    public static boolean isDisabledWorld(String worldName) {
        for (String disabledWorldName : plugin.getConf().getStringList("shops.disabled-worlds")) {
            if (worldName.equalsIgnoreCase(disabledWorldName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlacklistedName(String name) {
        String nameLowerCase = name.toLowerCase();
        for (String invalidName : plugin.getConf().getStringList("shops.blacklisted-names")) {
            if (nameLowerCase.contains(invalidName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static double parsePrice(double price) {
        boolean centsEnabled = plugin.getConf().getBoolean("shops.decimals.enabled");
        double maxDecimals = centsEnabled ? Math.pow(10, plugin.getConf().getInt("shops.decimals.max")) : 1;
        return Math.round(price * maxDecimals) / maxDecimals;
    }

    public static boolean isDoubleChest(Block block) {
        if (block.getType() != Material.CHEST)
            return false;

        boolean hasAdjacentChest = false;
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
            Block adjacent = block.getRelative(face);
            if (adjacent.getType() == Material.CHEST) {
                hasAdjacentChest = true;
                break;
            }
        }

        if (!hasAdjacentChest)
            return false;

        if (!(block.getState() instanceof Chest chest))
            return false;

        return chest.getInventory() instanceof DoubleChestInventory;
    }

    public static Block getAdjacentChest(Block block) {
        if (!(block.getState() instanceof Chest chest))
            return null;

        Inventory inv = chest.getInventory();
        if (!(inv instanceof DoubleChestInventory doubleInv))
            return null;

        Chest left = (Chest) doubleInv.getLeftSide().getHolder();
        Chest right = (Chest) doubleInv.getRightSide().getHolder();

        if (left.getBlock().equals(block))
            return right.getBlock();
        if (right.getBlock().equals(block))
            return left.getBlock();

        return null;
    }

    public static int getMaxShops(Player player) {
        int maxShops = plugin.getConf().getInt("shops.max-shops.default");
        if (maxShops == -1) {
            return -1;
        }
        for (String permission : plugin.getConf().getConfigurationSection("shops.max-shops").getKeys(false)) {
            if (permission.equals("default") || !player.hasPermission("spygchestshop.max." + permission)) {
                continue;
            }
            int value = plugin.getConf().getInt("shops.max-shops." + permission);
            if (value == -1) {
                return -1;
            }
            maxShops = Math.max(maxShops, value);
        }
        return maxShops;
    }
}
