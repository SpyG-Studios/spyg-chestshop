package com.spygstudios.chestshop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.spygstudios.chestshop.commands.handlers.CommandHandler;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.config.MessageConfig;
import com.spygstudios.chestshop.database.DatabaseShopManager;
import com.spygstudios.chestshop.gui.ChestShopGui.ChestShopHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopGuiHolder;
import com.spygstudios.chestshop.listeners.BreakListener;
import com.spygstudios.chestshop.listeners.BuildListener;
import com.spygstudios.chestshop.listeners.ChatListener;
import com.spygstudios.chestshop.listeners.ExplosionListener;
import com.spygstudios.chestshop.listeners.HopperListener;
import com.spygstudios.chestshop.listeners.InteractListener;
import com.spygstudios.chestshop.listeners.PlayerJoinListener;
import com.spygstudios.chestshop.listeners.gui.InventoryClickListener;
import com.spygstudios.chestshop.listeners.gui.InventoryCloseListener;
import com.spygstudios.chestshop.shop.yml.ShopYmlFile;
import com.spygstudios.spyglib.hologram.HologramManager;
import com.spygstudios.spyglib.version.VersionChecker;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;

public class ChestShop extends JavaPlugin {
    @Getter
    private static ChestShop instance;
    @Getter
    private Economy economy;
    @Getter
    private Config conf;
    @Getter
    private GuiConfig guiConfig;
    @Getter
    private HologramManager hologramManager;
    @Getter
    private CommandHandler commandHandler;
    @Getter
    @Setter
    private MessageConfig messageConfig;
    @Getter
    private DatabaseShopManager databaseShopManager;
    private static final String API_URL = "https://hangar.papermc.io/api/v1/projects/Spyg-ChestShop/latestrelease";

    public ChestShop() {
        instance = this;
    }

    @Override
    public void onEnable() {
        conf = new Config(this);
        guiConfig = new GuiConfig(this);
        messageConfig = new MessageConfig(this, conf.getString("locale"));
        Message.init(messageConfig);

        hologramManager = HologramManager.getManager(instance);
        commandHandler = new CommandHandler(instance);
        new InteractListener(this);
        new BreakListener(this);
        new BuildListener(this);
        new InventoryClickListener(instance);
        new InventoryCloseListener(instance);
        new ExplosionListener(instance);
        new ChatListener(instance);
        new HopperListener(instance);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            Entry<String, Boolean> versionInfo = VersionChecker.isLatestVersion(API_URL, getPluginMeta().getVersion());
            new PlayerJoinListener(instance, versionInfo.getKey(), versionInfo.getValue());
        });

        loadLocalizations();

        try {
            // bStats
            new Metrics(this, 24462);
        } catch (Exception e) {
        }

        getLogger().info("Loading economy plugin...");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault or economy plugin (e.g. Essentials) not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Loaded economy plugin: " + economy.getName());

        // Tárolási rendszer inicializálása a konfiguráció alapján
        String storageType = conf.getString("shops.storage-type", "sqlite").toLowerCase();

        if ("sqlite".equals(storageType)) {
            // SQLite adatbázis manager inicializálása
            databaseShopManager = new DatabaseShopManager(this);
            databaseShopManager.initialize().thenAccept(success -> {
                if (success) {
                    getLogger().info("SQLite shop rendszer sikeresen inicializálva");
                    databaseShopManager.startSaveScheduler();
                } else {
                    getLogger().severe("Hiba az SQLite shop rendszer inicializálása során!");
                    getServer().getPluginManager().disablePlugin(this);
                }
            });
        } else if ("mysql".equals(storageType)) {
            // MySQL adatbázis manager inicializálása
            String host = conf.getString("shops.mysql.host", "localhost");
            int port = conf.getInt("shops.mysql.port", 3306);
            String database = conf.getString("shops.mysql.database", "chestshop");
            String username = conf.getString("shops.mysql.username", "username");
            String password = conf.getString("shops.mysql.password", "password");

            databaseShopManager = new DatabaseShopManager(this, host, port, database, username, password);
            databaseShopManager.initialize().thenAccept(success -> {
                if (success) {
                    getLogger().info("MySQL shop rendszer sikeresen inicializálva");
                    databaseShopManager.startSaveScheduler();
                } else {
                    getLogger().severe("Hiba a MySQL shop rendszer inicializálása során!");
                    getServer().getPluginManager().disablePlugin(this);
                }
            });
        } else {
            // Régi YML rendszer használata
            getLogger().info("YML tárolás használata...");
            ShopYmlFile.loadShopFiles(instance);
            ShopYmlFile.startSaveScheduler(instance);
        }

        String info = String.format("%s v. %s plugin has been enabled!", getName(), getPluginMeta().getVersion());
        getLogger().info(info);
    }

    @Override
    public void onDisable() {
        if (commandHandler != null) {
            commandHandler.unregister();
        }

        // Tárolási rendszer mentése és bezárása
        String storageType = conf != null ? conf.getString("shops.storage-type", "sqlite").toLowerCase() : "sqlite";

        if (("sqlite".equals(storageType) || "mysql".equals(storageType)) && databaseShopManager != null) {
            databaseShopManager.saveShops();
            databaseShopManager.close();
        } else {
            // YML rendszer mentése
            ShopYmlFile.saveShops();
        }

        List<Object> guis = Arrays.asList(ChestShopHolder.class, PlayersHolder.class, ShopGuiHolder.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (guis.contains(holder.getClass())) {
                player.closeInventory();
            }
        }

        String info = String.format("%s v. %s plugin has been disabled!", getName(), getPluginMeta().getVersion());
        getLogger().info(info);
    }

    private void loadLocalizations() {
        File localeDirectory = new File(getDataFolder(), "locale");
        if (!localeDirectory.exists()) {
            localeDirectory.mkdirs();
        }

        try (JarFile jarFile = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("locale/") && !entry.isDirectory()) {
                    String fileName = name.substring("locale/".length());
                    File targetFile = new File(localeDirectory, fileName);
                    if (!targetFile.exists()) {
                        try (InputStream inputStream = getResource(name)) {
                            if (inputStream != null) {
                                Files.copy(inputStream, targetFile.toPath());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}