package com.spygstudios.chestshop.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.async.Async;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;

@Command(name = "spygchestshop remove", aliases = { "spcs remove", "chestshop remove", "scs remove" })
public class RemovePlayer {
    Config config;
    GuiConfig guiConfig;

    public RemovePlayer(ChestShop plugin) {
        config = plugin.getConf();
        guiConfig = plugin.getGuiConfig();
    }

    @Execute
    public void onRemove(@Context Player sender, @Arg Shop shop, @Async @Arg OfflinePlayer player) {
        if (sender.getUniqueId().equals(player.getUniqueId())) {
            Message.CANNOT_ADD_YOURSELF.send(sender);
            return;
        }

        shop.removePlayer(player.getUniqueId());
    }
}
