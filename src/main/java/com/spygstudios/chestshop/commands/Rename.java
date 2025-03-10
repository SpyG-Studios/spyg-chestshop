package com.spygstudios.chestshop.commands;

import java.util.Map;

import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop rename", aliases = { "spcs rename", "chestshop rename", "scs rename" })
public class Rename {
    Config config;

    public Rename(ChestShop plugin) {
        config = plugin.getConf();
    }

    @Execute
    @Permission("spygchestshop.use")
    public void onAdd(@Context Player player, @Arg Shop shop, @Arg String name) {
        if (shop.getName().equals(name)) {
            Message.SHOP_ALREADY_EXISTS.send(player, Map.of("%shop-name%", name));
            return;
        }
        if (ShopUtils.isBlacklistedName(name)) {
            Message.SHOP_BLACKLISTED_NAME.send(player);
            return;
        }
        int minLength = config.getInt("shops.name.min-length");
        int maxLength = config.getInt("shops.name.max-length");
        if (name.length() < minLength || name.length() > maxLength) {
            Message.SHOP_NAME_LENGTH.send(player, Map.of("%min-length%", minLength + "", "%max-length%", maxLength + ""));
            return;
        }
        Message.SHOP_RENAMED.send(player, Map.of("%old-name%", shop.getName(), "%new-name%", name));
        shop.setName(name);
    }
}
