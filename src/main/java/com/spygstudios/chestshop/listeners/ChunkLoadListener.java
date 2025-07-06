package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import com.spygstudios.chestshop.ChestShop;

public class ChunkLoadListener implements Listener {

    private final ChestShop plugin;

    public ChunkLoadListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getDataManager().loadShopsInChunk(event.getChunk());
    }

}
