package com.spygstudios.chestshop;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.spygstudios.chestshop.commands.handlers.CommandHandler;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.listeners.BreakListener;
import com.spygstudios.chestshop.listeners.BuildListener;
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

    public ChestShop() {
        instance = this;
    }

    @Override
    public void onEnable() {
        conf = new Config(this);
        guiConfig = new GuiConfig(this);
        Message.init(conf);
        commandHandler = new CommandHandler(instance);
        new InteractListener(this);
        new BreakListener(this);
        new BuildListener(this);
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
        String info = String.format("%s v. %s plugin has been enabled!", getName(), getPluginMeta().getVersion());
        getLogger().info(info);
    }

    @Override
    public void onDisable() {
        if (commandHandler != null) {
            commandHandler.unregister();
        }
        ShopFile.saveShops();

        List<Object> guis = Arrays.asList(ShopHolder.class, PlayersHolder.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (guis.contains(holder.getClass())) {
                player.closeInventory();
            }
        }
        String info = String.format("%s v. %s plugin has been disabled!", getName(), getPluginMeta().getVersion());
        getLogger().info(info);
    }

}
