package com.spygstudios.chestshop.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.shop.Shop;

public class ShopBreakListener implements Listener {

    private Config config;

    public ShopBreakListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = plugin.getConf();
    }

    @EventHandler
    public void onInteractWithShop(BlockBreakEvent event) {
        Player player = event.getPlayer();
        for (String disabledWorlds : config.getStringList("disabled-worlds")) {
            if (player.getWorld().getName().equalsIgnoreCase(disabledWorlds)) {
                return;
            }
        }
        Block block = event.getBlock();
        Location location = block.getLocation();
        Shop shop = Shop.getShop(location);
        if (shop == null) {
            return;
        }
        if (!shop.getOwner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (block.getType().equals(Material.OAK_WALL_SIGN)) {
            block.setType(Material.AIR);
        }
        shop.remove();
        player.sendMessage(config.getMessage("shop.removed"));
    }

}
