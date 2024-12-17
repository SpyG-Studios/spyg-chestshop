package com.spygstudios.chestshop;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.listeners.CommandListener;

import net.milkbowl.vault.economy.Economy;

public class ChestShop extends JavaPlugin {

    private static ChestShop instance;
    private Config config;
    private Economy economy;

    public void onEnable() {
        instance = this;
        config = new Config(this);
        new CommandListener(this, "chestshop");

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault or economy plugin (e.g. Essentials) not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();

        getLogger().info("<plugin> v. <version> plugin has been enabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

    public void onDisable() {
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
