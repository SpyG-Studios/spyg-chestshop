package com.spygstudios.chestshop.listeners;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.shop.Shop;

public class ChunkUnloadListener implements Listener {

    private final ChestShop plugin;

    public ChunkUnloadListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        List<Shop> shops = Shop.getShops();
        if (shops == null || shops.isEmpty()) {
            return;
        }
        for (Shop shop : shops) {
            if (!chunk.getWorld().equals(shop.getChestLocation().getWorld())) {
                continue;
            }
            int x = shop.getChestLocation().getBlockX() >> 4;
            int z = shop.getChestLocation().getBlockZ() >> 4;
            if (x != chunk.getX() || z != chunk.getZ()) {
                continue;
            }
            if (shop.isSaved()) {
                shop.unload();
            } else {
                plugin.getDataManager().saveShop(shop);
            }
        }
    }
}
