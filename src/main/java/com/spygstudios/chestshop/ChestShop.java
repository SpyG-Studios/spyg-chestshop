package com.spygstudios.chestshop;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuisConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.listeners.CommandListener;
import com.spygstudios.chestshop.listeners.ShopBreakListener;
import com.spygstudios.chestshop.listeners.ShopInteractListener;
import com.spygstudios.chestshop.shop.ShopFile;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;

public class ChestShop extends JavaPlugin {
    @Getter
    private static ChestShop instance;
    @Getter
    private Config conf;
    @Getter
    private Economy economy;

    @Getter
    private GuisConfig guisConfig;

    public void onEnable() {
        instance = this;
        conf = new Config(this);
        guisConfig = new GuisConfig(this);
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

        ShopFile.loadShopFiles(instance);

        Message.init(conf);
        ShopFile.startSaveScheduler(instance);
        getLogger().info("<plugin> v. <version> plugin has been enabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

    public void onDisable() {
        ShopFile.saveShops();
        getLogger().info("<plugin> v. <version> plugin has been disabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

}
