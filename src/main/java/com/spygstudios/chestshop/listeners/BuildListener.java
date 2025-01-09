package com.spygstudios.chestshop.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;

public class BuildListener implements Listener {

    public BuildListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (Shop.isDisabledWorld(player.getWorld())) {
            return;
        }
        Block block = event.getBlock();
        Block connectedChest = Shop.getAdjacentChest(block);
        if (connectedChest == null || (connectedChest != null && Shop.getShop(connectedChest.getLocation()) == null)) {
            return;
        }
        Shop shop = Shop.getShop(connectedChest.getLocation());
        if (shop.getOwnerId().equals(player.getUniqueId())) {
            return;
        }
        Message.SHOP_NOT_OWNER.send(player);

        event.setCancelled(true);
    }

}
