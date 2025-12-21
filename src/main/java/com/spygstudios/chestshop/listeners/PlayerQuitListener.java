package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.interfaces.SqlDataManager;

public class PlayerQuitListener implements Listener {

    private final ChestShop plugin;

    public PlayerQuitListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!(plugin.getDataManager() instanceof SqlDataManager dataManager)) {
            return;
        }
        dataManager.unloadPlayerShops(event.getPlayer().getUniqueId()).thenAccept(success -> {
            if (success) {
                plugin.getLogger().info("Successful unload check for player: " + event.getPlayer().getName());
            }
        });
    }

}
