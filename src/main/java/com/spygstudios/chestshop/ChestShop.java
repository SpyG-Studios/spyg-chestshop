package com.spygstudios.chestshop;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.listeners.CommandListener;
import com.spygstudios.chestshop.listeners.ShopBreakListener;
import com.spygstudios.chestshop.listeners.ShopInteractListener;
import com.spygstudios.chestshop.shop.ShopFile;

import net.milkbowl.vault.economy.Economy;

public class ChestShop extends JavaPlugin {

    private static ChestShop instance;
    private Config config;
    private Economy economy;

    public void onEnable() {
        instance = this;
        config = new Config(this);
        new CommandListener(this, "spygchestshop");
        new ShopInteractListener(this);
        new ShopBreakListener(this);

        getLogger().info("Loading economy plugin...");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault or economy plugin (e.g. Essentials) not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Loaded economy plugin: " + economy.getName());

        getLogger().info("Loading shops...");
        ShopFile.loadShopFiles(instance);
        getLogger().info("Shops loaded!");

        Message.init(config);
        ShopFile.startSaveScheduler(instance);
        getLogger().info("<plugin> v. <version> plugin has been enabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

    public void onDisable() {
        ShopFile.saveShops();
        getLogger().info("<plugin> v. <version> plugin has been disabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

    public Config getConf() {
        return config;
    }

    public Economy getEconomy() {
        return economy;
    }

    public static ChestShop getInstance() {
        return instance;
    }

}
