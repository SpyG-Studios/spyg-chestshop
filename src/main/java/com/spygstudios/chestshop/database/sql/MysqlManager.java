package com.spygstudios.chestshop.database.sql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.database.DatabaseHandler;
import com.spygstudios.chestshop.enums.DatabaseType;

public class MysqlManager extends DatabaseHandler {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MysqlManager(ChestShop plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.databaseType = DatabaseType.MYSQL;
        this.databaseFile = null;
        this.executor = Executors.newFixedThreadPool(4);

        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
                        host, port, database);
                connection = DriverManager.getConnection(url, username, password);
                createTables();
                plugin.getLogger().info("MySQL connection established: " + host + ":" + port + "/" + database);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to connect to MySQL database: " + host + ":" + port + "/" + database);
                return false;
            }
        }, executor);
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shops (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            owner_uuid VARCHAR(36) NOT NULL,
                            shop_name VARCHAR(255) NOT NULL,
                            price DECIMAL(15,2) NOT NULL DEFAULT 0,
                            material VARCHAR(255),
                            location TEXT NOT NULL,
                            created_at VARCHAR(50) NOT NULL,
                            do_notify BOOLEAN NOT NULL DEFAULT FALSE,
                            sold_items INT NOT NULL DEFAULT 0,
                            money_earned DECIMAL(15,2) NOT NULL DEFAULT 0,
                            UNIQUE KEY unique_shop (owner_uuid, shop_name),
                            INDEX idx_owner (owner_uuid),
                            INDEX idx_location (location(100))
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shop_players (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            shop_id INT NOT NULL,
                            player_uuid VARCHAR(36) NOT NULL,
                            FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
                            UNIQUE KEY unique_player_shop (shop_id, player_uuid),
                            INDEX idx_shop_id (shop_id),
                            INDEX idx_player_uuid (player_uuid)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);
        }
    }

}
