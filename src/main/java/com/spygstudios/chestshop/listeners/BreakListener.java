package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;

public class BreakListener implements Listener {

    public BreakListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (Shop.isDisabledWorld(event.getBlock().getWorld())) {
            return;
        }
        Shop shop = Shop.getShop(event.getBlock().getLocation());
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        boolean isAdmin = (player.hasPermission("spygchestshop.admin") || player.hasPermission("spygchestshop.admin.break")) && player.isSneaking();
        if (!shop.getOwnerId().equals(player.getUniqueId()) && !isAdmin) {
            Message.SHOP_NOT_OWNER.send(player);
            event.setCancelled(true);
            return;
        }
        if (!shop.getChestLocation().equals(event.getBlock().getLocation())) {
            return;
        }
        shop.remove();
        Message.SHOP_REMOVED.send(player, Map.of("%shop-name%", shop.getName()));
        event.setCancelled(true);
    }

}
