package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.components.ComponentUtils;

public class BreakListener implements Listener {

    public BreakListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteractWithShop(BlockBreakEvent event) {
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
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            Message.SHOP_NOT_OWNER.send(player);
            event.setCancelled(true);
            return;
        }
        shop.remove();
        player.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_REMOVED.get(), Map.of("%shop-name%", shop.getName())));
    }

}
