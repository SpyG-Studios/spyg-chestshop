package com.spygstudios.chestshop.interfaces;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;

import com.spygstudios.chestshop.shop.Shop;

public interface SqlDataManager extends DataManager {

    void loadShopsInChunk(Chunk chunk);

    void unloadShopsInChunk(Chunk chunk);

    CompletableFuture<Boolean> unloadPlayerShops(UUID ownerId);

    CompletableFuture<List<Shop>> loadPlayerShops(UUID ownerId);

}
