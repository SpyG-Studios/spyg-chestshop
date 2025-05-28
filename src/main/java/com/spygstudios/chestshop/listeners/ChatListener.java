package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.AmountHandler;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;

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
        TextComponent messageComponent = (TextComponent) event.message();
        String message = messageComponent.content();
        if (message.equalsIgnoreCase(config.getString("cancel"))) {
            pendingAmount.cancel();
            Message.ENTER_AMOUNT_CANCELLED.send(player);
            return;
        }

        try {
            int amount = Integer.parseInt(message);
            if (amount < 0 || amount > 10000000000000L) {
                Message.INVALID_NUMBER.send(player, Map.of("%entered%", message));
                return;
            }
            Bukkit.getScheduler().runTask(ChestShop.getInstance(), () -> pendingAmount.create(amount));
        } catch (NumberFormatException e) {
            Message.INVALID_NUMBER.send(player, Map.of("%entered%", message));
        }

    }
}
