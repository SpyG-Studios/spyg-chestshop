package com.spygstudios.chestshop.commands;

import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.async.Async;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop add", aliases = { "spcs add", "chestshop add", "scs add" })
public class AddPlayer {
    Config config;

    public AddPlayer(ChestShop plugin) {
        config = plugin.getConf();
    }

    @Execute
    @Permission("spygchestshop.use")
    @Permission("spygchestshop.*")
    public void onAdd(@Context Player sender, @Arg Shop shop, @Async @Arg OfflinePlayer player) {
        if (sender.getUniqueId().equals(player.getUniqueId())) {
            Message.CANT_ADD_YOURSELF.send(sender);
            return;
        }
        if (!player.isOnline() && !player.hasPlayedBefore()) {
            Message.PLAYER_NOT_PLAYED_BEFORE.send(sender, Map.of("%player-name%", player.getName()));
            return;
        }

        int maxPlayers = config.getInt("shops.max-players");
        if (maxPlayers != 0 && shop.getAddedPlayers().size() >= maxPlayers) {
            Message.SHOP_PLAYER_LIMIT_REACHED.send(sender, Map.of("%max-players%", String.valueOf(maxPlayers)));
            return;
        }

        shop.addPlayer(player.getUniqueId());
    }
}
