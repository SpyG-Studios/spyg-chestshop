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
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;

import net.milkbowl.vault.economy.EconomyResponse;

public class ShopInteractListener implements Listener {

    public ShopInteractListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
            Message.SHOP_NOT_OWNER.sendMessage(player);
            return;
        }
        if (shop.getMaterial() == null || shop.getAmount() == 0) {
            Message.SHOP_SETUP_NEEDED.sendMessage(player);
            return;
        }

        EconomyResponse response = ChestShop.getInstance().getEconomy().withdrawPlayer(player, shop.getPrice());
        if (response.transactionSuccess()) {
            ChestShop.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(shop.getOwner()), shop.getPrice());
            player.getInventory().addItem(new ItemStack(shop.getMaterial(), shop.getAmount()));
            Message.SHOP_BOUGHT.sendMessage(player, Map.of("%price%", String.valueOf(shop.getPrice()), "%material%", shop.getMaterial().name()));
            Player owner = Bukkit.getPlayer(shop.getOwner());
            if (shop.doNotify() && owner != null) {
                Message.SHOP_SOLD.sendMessage(owner, Map.of("%price%", String.valueOf(shop.getPrice()), "%material%", shop.getMaterial().name(), "%player-name%", player.getName()));
            }
            return;
        }
        Message.NOT_ENOUGH_MONEY.sendMessage(player, Map.of("%price%", String.valueOf(shop.getPrice())));
    }

}
