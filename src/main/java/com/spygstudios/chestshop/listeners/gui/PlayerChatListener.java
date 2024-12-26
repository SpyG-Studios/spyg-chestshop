package com.spygstudios.chestshop.listeners.gui;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.AmountHandler;
import com.spygstudios.spyglib.components.ComponentUtils;

import io.papermc.paper.event.player.AsyncChatEvent;

public class PlayerChatListener implements Listener {

    private final Config config;

    public PlayerChatListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = plugin.getConf();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        AmountHandler pendingAmount = AmountHandler.getPendingAmount(player);
        if (pendingAmount == null) {
            return;
        }

        event.setCancelled(true);
        String message = ComponentUtils.fromComponent(event.message());
        if (message.equalsIgnoreCase(config.getString("cancel"))) {
            Message.ENTER_AMOUNT_CANCELLED.sendMessage(player);
            pendingAmount.cancel();
            return;
        }

        double amount = -1;
        try {
            amount = Double.parseDouble(message);
        } catch (NumberFormatException e) {
            Message.INVALID_NUMBER.sendMessage(player, Map.of("%entered%", message));
            return;
        }

        if (amount < 0) {
            Message.INVALID_NUMBER.sendMessage(player, Map.of("%entered%", message));
            return;
        }
       

    }

}
