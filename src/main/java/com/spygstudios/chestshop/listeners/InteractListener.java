package com.spygstudios.chestshop.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.gui.ChestShopGui;
import com.spygstudios.chestshop.gui.ShopGui;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.item.ItemUtils;

public class InteractListener implements Listener {

    private final ChestShop plugin;

    public InteractListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
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
        if (player.isSneaking() && ItemUtils.hasItemInHand(player, Material.HOPPER)) {
            return;
        }

        // Owner
        boolean isAdmin = (player.hasPermission("spygchestshop.admin") || player.hasPermission("spygchestshop.admin.edit")) && player.isSneaking();
        if (shop.getOwnerId().equals(player.getUniqueId()) || isAdmin) {
            ChestShopGui.open(plugin, player, shop);
            event.setCancelled(true);
            return;
        }

        // Added player
        if (shop.getChestLocation().equals(location) && shop.getAddedPlayers().contains(player.getUniqueId())) {
            shop.openShopInventory(player);
            return;
        }

        // Buyer
        if (shop.getMaterial() == null) {
            Message.SHOP_SETUP_NEEDED.send(player);
            event.setCancelled(true);
            return;
        }
        ShopGui.open(plugin, player, shop);
        event.setCancelled(true);
    }

}
