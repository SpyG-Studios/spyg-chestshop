package com.spygstudios.chestshop.commands.admin;

import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.PageUtil;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.yml.ShopYmlFile;
import com.spygstudios.spyglib.components.ComponentUtils;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.async.Async;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

@Command(name = "spygchestshop admin list", aliases = { "spcs admin list", "chestshop admin list", "scs admin list" })
public class ShopListAdmin {

    @Execute
    @Permission("spygchestshop.admin.list")
    public void onList(@Context Player player, @Async @Arg OfflinePlayer target, @OptionalArg Integer page) {
        if (page == null) {
            page = 1;
        }

        ShopYmlFile file = ShopYmlFile.getShopFile(target.getUniqueId());
        if (file == null || file.getPlayerShops().isEmpty()) {
            Message.ADMIN_NO_SHOPS.send(player, Map.of("%player-name%", target.getName()));
            return;
        }

        Message.ADMIN_SHOP_LIST_HEAD.send(player, Map.of("%player-name%", target.getName()));
        List<Shop> shops = Shop.getShops(target.getUniqueId()).stream().sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).skip((page - 1) * 10L).limit(10).toList();
        for (Shop shop : shops) {
            Component hoverMessage = ComponentUtils.replaceComponent(Message.ADMIN_SHOP_LIST_SHOPS_HOVER.get(), Map.of(
                    "%shop-name%", shop.getName(),
                    "%material%", shop.getMaterialString(),
                    "%price%", shop.getPrice() + "",
                    "%items-left%", shop.getItemsLeft() + "",
                    "%location%", shop.getChestLocationString(),
                    "%created%", shop.getCreatedAt()));
            player.sendMessage(ComponentUtils.replaceComponent(Message.ADMIN_SHOP_LIST_SHOPS.get(), "%shop-name%", shop.getName()).hoverEvent(HoverEvent.showText(hoverMessage)));
        }

        player.sendMessage(PageUtil.getPages(page, file.getPlayerShops().size(), 10, "/spygchestshop list"));
    }
}
