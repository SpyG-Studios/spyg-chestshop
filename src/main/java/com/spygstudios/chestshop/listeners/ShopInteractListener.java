package com.spygstudios.chestshop.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.shop.Shop;

public class ShopInteractListener implements Listener {

    private Config config;

    public ShopInteractListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = plugin.getConf();
    }

    @EventHandler
    public void onInteractWithShop(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction().isLeftClick()) {
            return;
        }
        Player player = event.getPlayer();
        if (Shop.isDisabledWorld(player.getWorld())) {
            return;
        }
        Location location = event.getClickedBlock().getLocation();
        Shop shop = Shop.getShop(location);
        if (shop == null) {
            return;
        }
        if (shop.getChestLocation().equals(location) && !shop.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(config.getMessage("not-owner"));
            event.setCancelled(true);
            return;
        }
        if (shop.getSignLocation().equals(location)) {
            // shop.openShop(player);
            event.setCancelled(true);
        }
    }

}
