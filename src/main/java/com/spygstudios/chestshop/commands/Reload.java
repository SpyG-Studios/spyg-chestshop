package com.spygstudios.chestshop.commands;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop reload", aliases = { "spcs reload", "chestshop reload", "scs reload" })
public class Reload {
    Config config;
    GuiConfig guiConfig;

    public Reload(ChestShop plugin) {
        config = plugin.getConf();
        guiConfig = plugin.getGuiConfig();
    }

    @Execute
    @Permission("spygchestshop.admin.reload")
    public void onReload(@Context CommandSender player) {
        config.reloadConfig();
        guiConfig.reloadConfig();
        Message.CONFIG_RELOADED.send(player);
    }
}
