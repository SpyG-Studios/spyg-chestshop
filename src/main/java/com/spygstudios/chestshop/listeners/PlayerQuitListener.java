package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.gui.ShopGui;

public class PlayerQuitListener implements Listener {

    public PlayerQuitListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up player's shop viewing mode preference to prevent memory leaks
        ShopGui.clearPlayerMode(event.getPlayer());
    }
}