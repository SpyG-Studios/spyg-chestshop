package com.spygstudios.chestshop.database.sqlite;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.DatabaseHandler;
import com.spygstudios.chestshop.enums.DatabaseType;

public class SqLiteManager extends DatabaseHandler {

    public SqLiteManager(ChestShop plugin) {
        this.plugin = plugin;
        this.databaseType = DatabaseType.SQLITE;
        this.databaseFile = new File(plugin.getDataFolder(), "shops.db");
    }

    @Override
    public void initialize(Consumer<Boolean> callback) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);
                createTables();
                plugin.getLogger().info("SQLite database connection established: " + databaseFile.getAbsolutePath());
                scheduler.runTask(plugin, () -> callback.accept(true));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to connect to SQLite database: " + databaseFile.getAbsolutePath());
                scheduler.runTask(plugin, () -> callback.accept(false));
            }
        });
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shops (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            owner_uuid TEXT NOT NULL,
                            shop_name TEXT NOT NULL,
                            price REAL NOT NULL DEFAULT 0,
                            material TEXT,
                            location TEXT NOT NULL,
                            world TEXT NOT NULL,
                            x INTEGER NOT NULL,
                            y INTEGER NOT NULL,
                            z INTEGER NOT NULL,
                            chunk_x INTEGER NOT NULL,
                            chunk_z INTEGER NOT NULL,
                            created_at TEXT NOT NULL,
                            do_notify INTEGER NOT NULL DEFAULT 0,
                            sold_items INTEGER NOT NULL DEFAULT 0,
                            money_earned REAL NOT NULL DEFAULT 0,
                            UNIQUE(owner_uuid, shop_name)
                        );
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shop_players (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            shop_id INTEGER NOT NULL,
                            player_uuid TEXT NOT NULL,
                            FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
                            UNIQUE(shop_id, player_uuid)
                        )
                    """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_owner ON shops(owner_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_location ON shops(location)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shop_players_shop ON shop_players(shop_id)");
        }
    }
}
