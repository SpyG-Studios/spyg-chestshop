package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.spygstudios.chestshop.ChestShop;

public class ChunkUnloadListener implements Listener {

    private final ChestShop plugin;

    public ChunkUnloadListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getDataManager().unloadShopsInChunk(event.getChunk());
    }
}
