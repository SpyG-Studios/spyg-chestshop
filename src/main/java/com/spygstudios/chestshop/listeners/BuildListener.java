package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.components.ComponentUtils;

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
        Location location = block.getLocation();
        Shop shop = Shop.getShop(location);
        if (shop == null) {
            return;
        }

        boolean isAdmin = (player.hasPermission("spygchestshop.admin") || player.hasPermission("spygchestshop.admin.break")) && player.isSneaking();
        if (!shop.getOwnerId().equals(player.getUniqueId()) && !isAdmin) {
            Message.SHOP_NOT_OWNER.send(player);
            event.setCancelled(true);
            return;
        }
        shop.remove();
        player.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_REMOVED.get(), Map.of("%shop-name%", shop.getName())));
    }

}
