package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

public class ChunkUnloadListener implements Listener {

    private final ChestShop plugin;

    public ChunkUnloadListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getLogger().info(Shop.getShops().size() + " shops in total");
        DataManager dManager = plugin.getDataManager();
        dManager.getShopsInChunk(event.getChunk(), shops -> {
            for (Shop shop : shops) {
                shop.unload();
            }
            // if (shops == null || shops.isEmpty()) {
            // return;
            // }
            // for (Shop shop : shops) {
            // if (!event.getChunk().equals(shop.getChestLocation().getChunk()) ||
            // shop.getChestLocation().isChunkLoaded()) {
            // continue;
            // }
            // dManager.saveShop(shop, success -> {
            // if (success) {
            // shop.setSaved(true);
            // }
            // shop.unload();
            // plugin.getLogger().info("Unloaded shop: " + shop.getName() + " for owner: " +
            // shop.getOwnerId());
            // });
            // }
        });
    }
}
