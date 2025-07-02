package com.spygstudios.chestshop.commands.admin;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop migrate", aliases = { "spcs migrate", "chestshop migrate", "scs migrate" })
public class Migrate {
    
    private final ChestShop plugin;

    public Migrate(ChestShop plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.admin.migrate")
    public void onMigrate(@Context CommandSender sender) {
        sender.sendMessage("§6Migráció indítása YML-ről adatbázisra...");
        
        plugin.getDatabaseShopManager().forceMigration().thenAccept(success -> {
            if (success) {
                sender.sendMessage("§aMigráció sikeresen befejezve!");
            } else {
                sender.sendMessage("§cHiba történt a migráció során! Ellenőrizd a konzolt!");
            }
        }).exceptionally(throwable -> {
            sender.sendMessage("§cVégrehajtási hiba a migráció során: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }
}
