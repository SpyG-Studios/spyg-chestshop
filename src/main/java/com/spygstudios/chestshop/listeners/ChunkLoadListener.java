package com.spygstudios.chestshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.interfaces.SqlDataManager;

public class ChunkLoadListener implements Listener {

    private final ChestShop plugin;

    public ChunkLoadListener(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!(plugin.getDataManager() instanceof SqlDataManager dataManager)) {
            return;
        }
        dataManager.loadShopsInChunk(event.getChunk());
    }

}
