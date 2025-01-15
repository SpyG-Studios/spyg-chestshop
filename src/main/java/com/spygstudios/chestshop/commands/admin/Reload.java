package com.spygstudios.chestshop.commands.admin;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
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

    public Reload(ChestShop plugin) {
        this.config = plugin.getConf();
        this.guiConfig = plugin.getGuiConfig();
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.admin.reload")
    public void onReload(@Context CommandSender player) {
        config.reloadConfig();
        guiConfig.reloadConfig();
        for (Shop shop : Shop.getShops()) {
            shop.updateHologramRows();
        }
        Message.CONFIG_RELOADED.send(player);
    }
}
