package com.spygstudios.chestshop.shop;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.inventory.InventoryUtils;

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

        BlockData data = block.getBlockData();
        if (!(data instanceof Chest chestData))
            return false;

        return chestData.getType() != Chest.Type.SINGLE;
    }

    public static Block getAdjacentChest(Block block) {
        if (block.getType() != Material.CHEST)
            return null;

        BlockData data = block.getBlockData();
        if (!(data instanceof Directional)) {
            return null;
        }
        if (!(data instanceof Chest chestData)) {
            return null;
        }

        if (chestData.getType() == Type.SINGLE) {
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

    private static BlockFace getConnectedChestOffset(BlockFace facing, Chest.Type type) {
        return switch (facing) {
            case NORTH -> (type == Type.RIGHT ? BlockFace.WEST : BlockFace.EAST);
            case SOUTH -> (type == Type.RIGHT ? BlockFace.EAST : BlockFace.WEST);
            case WEST -> (type == Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH);
            case EAST -> (type == Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH);
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

    public static int getSellableItemCount(Inventory inventory, ItemStack item) {
        int itemCount = InventoryUtils.countItems(inventory, i -> {
            if (!ShopUtils.isSimilar(item, i)) {
                return false;
            }
            return isDurabilitySufficient(i);
        });

        return itemCount;
    }

    public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        if (item1.getItemMeta() instanceof Damageable d1 && item2.getItemMeta() instanceof Damageable d2) {
            ItemStack copy1 = item1.clone();
            ItemStack copy2 = item2.clone();

            d1.setDamage(0);
            d2.setDamage(0);
            copy1.setItemMeta(d1);
            copy2.setItemMeta(d2);
            return copy1.isSimilar(copy2);
        }
        return item1.isSimilar(item2);
    }

    private static boolean isDurabilitySufficient(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable damageable && damageable.hasDamage()) {
            short maxDurability = item.getType().getMaxDurability();
            int damageInPercent = (int) (Math.ceil((double) damageable.getDamage() / maxDurability * 100));
            int durabilityInPercent = 100 - damageInPercent;
            int minDurabilityPercent = plugin.getConf().getInt("shops.minimum-durability");
            if (minDurabilityPercent > durabilityInPercent) {
                return false;
            }
        }
        return true;
    }

    public static int extractItems(Inventory fromInventory, Inventory toInventory, ItemStack item, int itemCount) {
        int extractedItems = 0;
        for (ItemStack chestItem : fromInventory.getContents()) {
            if (itemCount <= 0)
                break;

            if (chestItem != null && ShopUtils.isSimilar(chestItem, item)) {
                if (!isDurabilitySufficient(chestItem)) {
                    continue;
                }
                int chestAmount = chestItem.getAmount();
                int removeAmount = Math.min(itemCount, chestAmount);

                ItemStack clone = chestItem.clone();
                clone.setAmount(removeAmount);

                HashMap<Integer, ItemStack> leftover = toInventory.addItem(clone);

                if (leftover.isEmpty()) {
                    chestItem.setAmount(chestAmount - removeAmount);
                    itemCount -= removeAmount;
                    extractedItems += removeAmount;
                } else {
                    int added = removeAmount - leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    chestItem.setAmount(chestAmount - added);
                    itemCount -= added;
                    extractedItems += added;
                    break;
                }
            }
        }

        return extractedItems;
    }
}
