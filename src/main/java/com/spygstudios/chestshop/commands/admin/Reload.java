package com.spygstudios.chestshop.commands.admin;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.config.MessageConfig;
import com.spygstudios.chestshop.shop.Shop;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop reload", aliases = { "spcs reload", "chestshop reload", "scs reload" })
public class Reload {
    Config config;
    GuiConfig guiConfig;
    ChestShop plugin;
    MessageConfig messageConfig;

    public Reload(ChestShop plugin) {
        this.config = plugin.getConf();
        this.guiConfig = plugin.getGuiConfig();
        this.messageConfig = plugin.getMessageConfig();
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.admin.reload")
    public void onReload(@Context CommandSender player) {
        config.reloadConfig();
        guiConfig.reloadConfig();
        plugin.getMessageConfig().reloadConfig();
        for (Shop shop : Shop.getShops()) {
            shop.updateHologramRows();
        }
        if (!config.getString("locale").equals(plugin.getMessageConfig().getLocale())) {
            plugin.setMessageConfig(new MessageConfig(plugin, config.getString("locale")));
            Message.init(plugin.getMessageConfig());
        }
        Message.CONFIG_RELOADED.send(player);
    }
}
