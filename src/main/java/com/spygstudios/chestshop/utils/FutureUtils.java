package com.spygstudios.chestshop.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class for handling CompletableFuture operations with proper thread
 * management.
 * Ensures that future completion happens on the main thread to maintain thread
 * safety.
 */
public class FutureUtils {

    public static <T> void completeSync(Plugin plugin, CompletableFuture<T> future, T value) {
        Bukkit.getScheduler().runTask(plugin, () -> future.complete(value));
    }

    public static <T> void completeSync(Plugin plugin, CompletableFuture<T> future, Supplier<T> supplier) {
        T value = supplier.get();
        completeSync(plugin, future, value);
    }

    public static <T> void completeSyncExceptionally(Plugin plugin, CompletableFuture<T> future, Throwable throwable) {
        Bukkit.getScheduler().runTask(plugin, () -> future.completeExceptionally(throwable));
    }

    public static <T> CompletableFuture<T> completedSyncFuture(Plugin plugin, T value) {
        CompletableFuture<T> future = new CompletableFuture<>();
        completeSync(plugin, future, value);
        return future;
    }

    public static <T> CompletableFuture<T> runTaskAsync(Plugin plugin, Supplier<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                T result = task.get();
                completeSync(plugin, future, result);
            } catch (Exception e) {
                completeSyncExceptionally(plugin, future, e);
            }
        });
        return future;
    }
}
