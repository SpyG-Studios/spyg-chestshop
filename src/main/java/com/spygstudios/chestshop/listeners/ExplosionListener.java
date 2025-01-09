package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.components.ComponentUtils;

public class ExplosionListener implements Listener {
    Config config;

    public ExplosionListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        config = plugin.getConf();
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        if (config.getBoolean("shop.anti-explosion")) {
            event.blockList().removeIf(block -> Shop.getShop(block.getLocation()) != null);
            return;
        }
        event.blockList().stream().filter(block -> Shop.getShop(block.getLocation()) != null).forEach(block -> {
            Shop shop = Shop.getShop(block.getLocation());
            if (Bukkit.getPlayer(shop.getOwnerId()) != null) {
                Bukkit.getPlayer(shop.getOwnerId())
                        .sendMessage(ComponentUtils.replaceComponent(Message.SHOP_EXPLODED.get(), Map.of("%shop-name%", shop.getName(), "%shop-location%", shop.getChestLocationString())));
            }
            shop.remove();
        });
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        if (config.getBoolean("shop.anti-explosion")) {
            event.blockList().removeIf(block -> Shop.getShop(block.getLocation()) != null);
            return;
        }
        event.blockList().stream().filter(block -> Shop.getShop(block.getLocation()) != null).forEach(block -> {
            Shop shop = Shop.getShop(block.getLocation());
            if (Bukkit.getPlayer(shop.getOwnerId()) != null) {
                Bukkit.getPlayer(shop.getOwnerId())
                        .sendMessage(ComponentUtils.replaceComponent(Message.SHOP_EXPLODED.get(), Map.of("%shop-name%", shop.getName(), "%shop-location%", shop.getChestLocationString())));
            }
            shop.remove();
        });
    }
}
