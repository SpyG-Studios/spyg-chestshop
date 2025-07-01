package com.spygstudios.chestshop.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.spygstudios.chestshop.PageUtil;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;
import com.spygstudios.spyglib.components.ComponentUtils;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

@Command(name = "spygchestshop list", aliases = { "spcs list", "chestshop list", "scs list" })
public class ShopList {

    @Execute
    @Permission("spygchestshop.use")
    public void onList(@Context Player player, @OptionalArg Integer page) {
        if (page == null) {
            page = 1;
        }

        ShopFile file = ShopFile.getShopFile(player);
        if (file == null || file.getPlayerShops().isEmpty()) {
            Message.SHOP_NO_SHOPS.send(player);
            return;
        }

        Message.SHOP_LIST_HEAD.send(player);
        List<Shop> shops = Shop.getShops(player).stream().sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).skip((page - 1) * 10L).limit(10).toList();
        for (Shop shop : shops) {
            Component hoverMessage = ComponentUtils.replaceComponent(Message.SHOP_LIST_SHOPS_HOVER.get(), Map.of(
                    "%shop-name%", shop.getName(),
                    "%material%", shop.getMaterialString(),
                    "%price%", shop.getPrice() + "",
                    "%items-left%", shop.getItemsLeft() + "",
                    "%location%", shop.getChestLocationString(),
                    "%created%", shop.getCreatedAt()));
            player.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_LIST_SHOPS.get(), "%shop-name%", shop.getName()).hoverEvent(HoverEvent.showText(hoverMessage)));
        }

        player.sendMessage(PageUtil.getPages(page, file.getPlayerShops().size(), 10, "/spygchestshop list"));
    }
}
