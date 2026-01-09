package com.spygstudios.chestshop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
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
import com.spygstudios.chestshop.database.yaml.YamlStorage;
import com.spygstudios.chestshop.gui.DashboardGui.DashboardHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.listeners.BreakListener;
import com.spygstudios.chestshop.listeners.BuildListener;
import com.spygstudios.chestshop.listeners.ChatListener;
import com.spygstudios.chestshop.listeners.ChunkLoadListener;
import com.spygstudios.chestshop.listeners.ChunkUnloadListener;
import com.spygstudios.chestshop.listeners.ExplosionListener;
import com.spygstudios.chestshop.listeners.HopperListener;
import com.spygstudios.chestshop.listeners.InteractListener;
import com.spygstudios.chestshop.listeners.PlayerJoinListener;
import com.spygstudios.chestshop.listeners.PlayerQuitListener;
import com.spygstudios.chestshop.listeners.gui.DashboardGuiHandler;
import com.spygstudios.chestshop.listeners.gui.InventoryCloseListener;
import com.spygstudios.chestshop.listeners.gui.InventoryDragListener;
import com.spygstudios.chestshop.listeners.gui.PlayerGuiHandler;
import com.spygstudios.chestshop.listeners.gui.ShopGuiHandler;
import com.spygstudios.spyglib.hologram.HologramManager;
import com.spygstudios.spyglib.version.VersionChecker;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.milkbowl.vault.economy.Economy;

@Getter
public class ChestShop extends JavaPlugin {
    @Getter
    private static ChestShop instance;
    private Economy economy;
    private Config conf;
    private GuiConfig guiConfig;
    private HologramManager hologramManager;
    private CommandHandler commandHandler;
    @Setter
    private MessageConfig messageConfig;
    @Setter
    private DataManager dataManager;
    private boolean latestVersion = true;
    private String currentVersion;
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

        hologramManager = HologramManager.getManager(this);
        commandHandler = new CommandHandler(this);
        new InteractListener(this);
        new BreakListener(this);
        new BuildListener(this);
        new DashboardGuiHandler(instance);
        new PlayerGuiHandler(instance);
        new ShopGuiHandler(instance);
        new InventoryCloseListener(instance);
        new ExplosionListener(instance);
        new ChatListener(instance);
        new HopperListener(instance);
        new PlayerJoinListener(this);
        new PlayerQuitListener(this);
        new ChunkLoadListener(this);
        new ChunkUnloadListener(this);
        new InventoryDragListener(this);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            Entry<String, Boolean> versionInfo = VersionChecker.isLatestVersion(API_URL, getPluginMeta().getVersion());
            this.currentVersion = versionInfo.getKey();
            this.latestVersion = versionInfo.getValue();
        });

        loadLocalizations();

        try {
            // bStats
            new Metrics(this, 24462);
        } catch (Exception e) {
        }

        getLogger().info("Looking for economy plugin...");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault or economy plugin (e.g. Essentials) not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Found economy plugin: " + economy.getName());

        String storageType = conf.getString("storage-type");
        getLogger().info("Using " + storageType + " storage type.");
        this.dataManager = createDataManager(storageType);
        if (dataManager == null) {
            getLogger().severe("Invalid storage type in config! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            dataManager.initialize().thenAccept(success -> {
                if (!success) {
                    getLogger().severe("Failed to initialize data manager! Disabling plugin...");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            }).join();
            dataManager.startSaveScheduler();
        });

        String info = String.format("%s v. %s plugin has been enabled!", getName(), getPluginMeta().getVersion());
        getLogger().info(info);
    }

    public DataManager createDataManager(String type) {
        DataManager dataManager = new YamlStorage(this);
        // switch (type) {
        // case "yaml":
        // dataManager = new YamlStorage(this);
        // break;
        // case "mysql":
        // String host = conf.getString("mysql.host");
        // int port = conf.getInt("mysql.port");
        // String database = conf.getString("mysql.database");
        // String username = conf.getString("mysql.username");
        // String password = conf.getString("mysql.password");
        // if (host == null || database == null || username == null || password == null)
        // {
        // getLogger().severe("MySQL configuration is incomplete! Disabling plugin...");
        // getServer().getPluginManager().disablePlugin(this);
        // return null;
        // }
        // dataManager = new MysqlStorage(this, host, port, database, username,
        // password);
        // break;
        // case "sqlite":
        // dataManager = new SqliteStorage(this);
        // break;
        // }
        return dataManager;
    }

    @Override
    public void onDisable() {
        if (commandHandler != null) {
            commandHandler.unregister();
        }
        if (dataManager != null) {
            dataManager.close();
        }

        List<Object> guis = Arrays.asList(DashboardHolder.class, PlayersHolder.class, ShopHolder.class);
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

    public String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    public byte[] stringToBytes(String str) {
        if (str == null) {
            return null;
        }
        return Base64.getDecoder().decode(str);
    }

    public String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }

    public String extractContent(Component component) {
        StringBuilder sb = new StringBuilder();

        if (component instanceof TextComponent text) {
            sb.append(text.content());
        }

        for (Component child : component.children()) {
            sb.append(extractContent(child));
        }

        return sb.toString();
    }

}