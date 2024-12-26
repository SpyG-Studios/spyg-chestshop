package com.spygstudios.chestshop.commands;

import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;

@Command(name = "spygchestshop reload", aliases = { "spcs reload", "chestshop reload", "scs reload" })
public class Reload {
    Config config;
    GuiConfig guiConfig;

    public Reload(ChestShop plugin) {
        config = plugin.getConf();
        guiConfig = plugin.getGuiConfig();
    }

    @Execute
    public void onReload(@Context Player player) {
        config.reloadConfig();
        guiConfig.reloadConfig();
        Message.CONFIG_RELOADED.sendMessage(player);
    }
}
