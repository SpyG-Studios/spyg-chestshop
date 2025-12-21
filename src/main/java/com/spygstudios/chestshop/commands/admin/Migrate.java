package com.spygstudios.chestshop.commands.admin;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.literal.Literal;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop migrate", aliases = { "spcs migrate", "chestshop migrate", "scs migrate" })
public class Migrate {
    private final ChestShop plugin;

    public Migrate(ChestShop plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.*")
    @Permission("spygchestshop.admin.*")
    @Permission("spygchestshop.admin.migrate")
    public void onMigrate(@Context CommandSender sender, @Literal({ "mysql", "sqlite", "yaml" }) String targetType) {
        String currentType = plugin.getConf().getString("storage-type");

        if (currentType.equalsIgnoreCase(targetType)) {
            Message.MIGRATE_SAME_TYPE.send(sender, Map.of("%storage-type%", targetType));
            return;
        }

        if (!targetType.equalsIgnoreCase("yaml") && !targetType.equalsIgnoreCase("sqlite") && !targetType.equalsIgnoreCase("mysql")) {
            Message.MIGRATE_INVALID_TYPE.send(sender);
            return;
        }

        Message.MIGRATE_IN_PROGRESS.send(sender, Map.of("%old-type%", currentType, "%new-type%", targetType));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 1. Betöltjük az összes shop-ot a régi tárolóból
                List<Shop> allShops = plugin.getDataManager().getAllShops().join();
                if (allShops == null) {
                    Message.MIGRATE_ERROR_LOADING.send(sender);
                    return;
                }
                Message.MIGRATE_COLLECTING.send(sender, Map.of("%count%", String.valueOf(allShops.size())));

                // 2. Új tárolási mód létrehozása (a konstruktor már inicializálja)
                DataManager newDataManager = plugin.createDataManager(targetType);
                if (newDataManager == null) {
                    Message.MIGRATE_ERROR_NEW_STORAGE.send(sender);
                    return;
                }
                newDataManager.initialize().thenAccept(success -> {
                    if (!success) {
                        plugin.getLogger().severe("Failed to initialize data manager! Disabling plugin...");
                        plugin.getServer().getPluginManager().disablePlugin(plugin);
                        return;
                    }
                }).join();
                newDataManager.startSaveScheduler();

                Message.MIGRATE_SAVING.send(sender);

                // 4. Shop-ok mentése az új tárolóba
                int successCount = 0;
                int failCount = 0;
                System.out.println("Starting migration of " + allShops.size() + " shops from " + currentType + " to " + targetType);
                for (Shop shop : allShops) {
                    try {
                        boolean success = newDataManager.saveShop(shop).join();
                        if (success) {
                            successCount++;
                        } else {
                            failCount++;
                            plugin.getLogger().warning("Error saving shop to new storage: " + shop.getName() + " (Owner: " + shop.getOwnerId() + ")");
                        }
                    } catch (Exception e) {
                        failCount++;
                        plugin.getLogger().severe("Exception saving shop to new storage: " + shop.getName() + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                Message.MIGRATE_SAVED.send(sender, Map.of("%count%", String.valueOf(successCount)));
                if (failCount > 0) {
                    Message.MIGRATE_FAILED.send(sender, Map.of("%count%", String.valueOf(failCount)));
                }

                // 5. Váltás az új tárolóra (szinkron kontextusban)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // 5a. Régi tároló bezárása
                    DataManager oldDataManager = plugin.getDataManager();
                    if (oldDataManager != null) {
                        oldDataManager.close();
                        Message.MIGRATE_OLD_CLOSED.send(sender, Map.of("%storage-type%", currentType));
                    }

                    // 5b. Új tároló beállítása
                    plugin.setDataManager(newDataManager);
                    newDataManager.startSaveScheduler();

                    // 5c. Shop-ok memóriából törlése
                    for (Shop shop : allShops) {
                        shop.unload();
                    }

                    // 5d. Shop-ok újratöltése az online játékosok számára
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        newDataManager.getPlayerShops(player.getUniqueId());
                    });

                    // 5e. Config frissítése
                    plugin.getConf().set("storage-type", targetType);
                    plugin.getConf().saveConfig();

                    Message.MIGRATE_COMPLETE.send(sender);
                    Message.MIGRATE_NEW_STORAGE.send(sender, Map.of("%storage-type%", targetType));
                });

            } catch (Exception e) {
                Message.MIGRATE_CRITICAL_ERROR.send(sender);
                plugin.getLogger().severe("Error during migration: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
