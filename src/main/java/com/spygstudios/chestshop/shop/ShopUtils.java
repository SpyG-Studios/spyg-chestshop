package com.spygstudios.chestshop.shop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;

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
        if (block.getType() != Material.CHEST)
            return null;

        BlockData data = block.getBlockData();
        if (!(data instanceof Directional)) {
            return null;
        }
        if (!(data instanceof org.bukkit.block.data.type.Chest chestData)) {
            return null;
        }

        if (chestData.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE) {
            return null;
        }

        BlockFace facing = chestData.getFacing();

        BlockFace offset = getConnectedChestOffset(facing, chestData.getType());
        if (offset == null) {
            return null;
        }

        Block otherBlock = block.getRelative(offset);
        if (otherBlock.getType() == Material.CHEST)
            return otherBlock;
        return null;
    }

    private static BlockFace getConnectedChestOffset(BlockFace facing, org.bukkit.block.data.type.Chest.Type type) {
        return switch (facing) {
        case NORTH -> (type == org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.WEST : BlockFace.EAST);
        case SOUTH -> (type == org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.EAST : BlockFace.WEST);
        case WEST -> (type == org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH);
        case EAST -> (type == org.bukkit.block.data.type.Chest.Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH);
        default -> null;
        };
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
