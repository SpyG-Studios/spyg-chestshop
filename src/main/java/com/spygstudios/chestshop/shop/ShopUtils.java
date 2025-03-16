package com.spygstudios.chestshop.shop;

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

    public static boolean isDoubleChest(Block block) {
        if (!(block.getState() instanceof Chest chest)) {
            return false;
        }
        Inventory inv = chest.getInventory();
        return inv instanceof DoubleChestInventory;
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
