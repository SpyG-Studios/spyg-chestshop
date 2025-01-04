package com.spygstudios.chestshop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.spygstudios.chestshop.commands.CommandHandler;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.listeners.BreakListener;
import com.spygstudios.chestshop.listeners.ChatListener;
import com.spygstudios.chestshop.listeners.ExplosionListener;
import com.spygstudios.chestshop.listeners.InteractListener;
import com.spygstudios.chestshop.listeners.SignListener;
import com.spygstudios.chestshop.listeners.gui.InventoryClickListener;
import com.spygstudios.chestshop.listeners.gui.InventoryCloseListener;
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
    private GuiConfig guiConfig;

    @Getter
    private CommandHandler commandHandler;

    public void onEnable() {
        instance = this;
        conf = new Config(this);
        guiConfig = new GuiConfig(this);
        Message.init(conf);
        commandHandler = new CommandHandler(instance);
        new InteractListener(this);
        new BreakListener(this);
        new InventoryClickListener(instance);
        new InventoryCloseListener(instance);
        new ExplosionListener(instance);
        new SignListener(instance);
        new ChatListener(instance);

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

        ShopFile.startSaveScheduler(instance);
        getLogger().info("<plugin> v. <version> plugin has been enabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

    public void onDisable() {
        if (commandHandler != null) {
            commandHandler.unregister();
        }
        ShopFile.saveShops();

        List<Object> guis = new ArrayList<>() {
            {
                add(ShopHolder.class);
            }
        };
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            System.out.println(holder.getClass());
            if (guis.contains(holder.getClass())) {
                player.closeInventory();
            }
        }
        getLogger().info("<plugin> v. <version> plugin has been disabled!".replace("<plugin>", getName()).replace("<version>", getPluginMeta().getVersion()));
    }

}
