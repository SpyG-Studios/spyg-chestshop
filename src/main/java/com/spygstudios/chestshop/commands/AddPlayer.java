package com.spygstudios.chestshop.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.components.ComponentUtils;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.async.Async;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;

@Command(name = "spygchestshop add", aliases = { "spcs add", "chestshop add", "scs add" })
public class AddPlayer {
    Config config;

    public AddPlayer(ChestShop plugin) {
        config = plugin.getConf();
    }

    @Execute
    public void onAdd(@Context Player sender, @Arg Shop shop, @Async @Arg OfflinePlayer player) {
        if (sender.getUniqueId().equals(player.getUniqueId())) {
            Message.CANNOT_ADD_YOURSELF.send(sender);
            return;
        }
        if (!player.isOnline() && !player.hasPlayedBefore()) {
            sender.sendMessage(ComponentUtils.replaceComponent(Message.PLAYER_NOT_PLAYED_BEFORE.get(), "%player-name%", player.getName()));
            return;
        }
        if (shop.getAddedPlayers().size() >= config.getInt("shops.max-players")) {
            sender.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_PLAYER_LIMIT_REACHED.get(), "%max-players%", String.valueOf(config.getInt("shops.max-players"))));
            return;
        }

        shop.addPlayer(player.getUniqueId());
    }
}
