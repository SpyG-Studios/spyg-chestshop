package com.spygstudios.chestshop.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;

public class BuildListener implements Listener {

    public BuildListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getBlock().getType().equals(Material.CHEST)) {
            return;
        }

        Player player = event.getPlayer();
        if (ShopUtils.isDisabledWorld(event.getBlock().getWorld().getName())) {
            return;
        }
        Chest chest = (Chest) event.getBlock().getState().getBlockData();
        Type type = chest.getType();
        if (type.equals(Type.SINGLE)) {
            return;
        }

        Block block = event.getBlock();
        Block adjacentBlock = null;
        switch (chest.getFacing()) {
            case NORTH:
                adjacentBlock = block.getRelative(type == Type.LEFT ? BlockFace.EAST : BlockFace.WEST);
                break;
            case EAST:
                adjacentBlock = block.getRelative(type == Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH);
                break;
            case SOUTH:
                adjacentBlock = block.getRelative(type == Type.LEFT ? BlockFace.WEST : BlockFace.EAST);
                break;
            case WEST:
                adjacentBlock = block.getRelative(type == Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH);
                break;
            default:
                event.setCancelled(true);
                return;
        }
        Shop shop = Shop.getShop(adjacentBlock.getLocation());
        if (shop == null) {
            return;
        }

        if (shop.getOwnerId().equals(player.getUniqueId())) {
            return;
        }

        Message.SHOP_NOT_OWNER.send(player);

        event.setCancelled(true);
    }

}
