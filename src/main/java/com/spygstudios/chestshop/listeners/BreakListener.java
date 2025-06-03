package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.ShopRemoveCause;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;

public class BreakListener implements Listener {

    public BreakListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (ShopUtils.isDisabledWorld(event.getBlock().getWorld().getName())) {
            return;
        }
        if (!event.getBlock().getType().equals(Material.CHEST)) {
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
        shop.remove(event.getPlayer(), ShopRemoveCause.PLAYER);
        Message.SHOP_REMOVED.send(player, Map.of("%shop-name%", shop.getName()));
        event.setCancelled(true);
    }

}
