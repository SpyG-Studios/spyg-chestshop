package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.components.ComponentUtils;

import net.milkbowl.vault.economy.EconomyResponse;

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

        // Owner
        if (shop.getOwner().equals(player.getUniqueId())) {
            if (shop.getChestLocation().equals(location)) {
                return;
            }
            // shop.openShop(player);
            event.setCancelled(true);
            return;
        }

        // Buyer
        event.setCancelled(true);
        if (shop.getChestLocation().equals(location)) {
            player.sendMessage(config.getMessage("shop.not-owner"));
            return;
        }
        if (shop.getMaterial() == null || shop.getAmount() == 0) {
            player.sendMessage(config.getMessage("shop.setup-needed"));
            return;
        }

        EconomyResponse response = ChestShop.getInstance().getEconomy().withdrawPlayer(player, shop.getPrice());
        if (response.transactionSuccess()) {
            ChestShop.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(shop.getOwner()), shop.getPrice());
            player.getInventory().addItem(new ItemStack(shop.getMaterial(), shop.getAmount()));
            player.sendMessage(ComponentUtils.replaceComponent(config.getMessage("shop.bought"), Map.of("%price%", String.valueOf(shop.getPrice()), "%material%", shop.getMaterial().name())));
            if (shop.doNotify() && Bukkit.getPlayer(shop.getOwner()) != null) {
                Bukkit.getPlayer(shop.getOwner()).sendMessage(ComponentUtils.replaceComponent(config.getMessage("shop.sold"),
                        Map.of("%price%", String.valueOf(shop.getPrice()), "%material%", shop.getMaterial().name(), "%player-name%", player.getName())));
            }
            return;
        }
        player.sendMessage(ComponentUtils.replaceComponent(config.getMessage("not-enough-money"), Map.of("%price%", String.valueOf(shop.getPrice()))));

    }

}
