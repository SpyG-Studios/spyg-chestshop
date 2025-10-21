package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.interfaces.SqlDataManager;

public class ChunkUnloadListener implements Listener {

    private final ChestShop plugin;

    public ChunkUnloadListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!(plugin.getDataManager() instanceof SqlDataManager dataManager)) {
            return;
        }
        dataManager.unloadShopsInChunk(event.getChunk());
    }
}
