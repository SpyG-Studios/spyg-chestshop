package com.spygstudios.chestshop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.AmountHandler;
import com.spygstudios.spyglib.components.ComponentUtils;

import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatListener implements Listener {
    Config config;

    public ChatListener(ChestShop plugin) {
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
            pendingAmount.cancel();
            Message.ENTER_AMOUNT_CANCELLED.sendMessage(player);
            return;
        }

        try {
            int amount = Integer.parseInt(message);
            if (amount < 0 || amount > 10000000000000L) {
                player.sendMessage(ComponentUtils.replaceComponent(Message.INVALID_NUMBER.get(), "%entered%", message));
                return;
            }
            Bukkit.getScheduler().runTask(ChestShop.getInstance(), () -> pendingAmount.create(amount));
        } catch (NumberFormatException e) {
            player.sendMessage(ComponentUtils.replaceComponent(Message.INVALID_NUMBER.get(), "%entered%", message));

        }

    }
}
