package com.spygstudios.chestshop.commands.admin;

import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.utils.FormatUtils;
import com.spygstudios.chestshop.utils.PageUtil;
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

    private final ChestShop plugin;

    public ShopListAdmin(ChestShop plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.admin.list")
    @Permission("spygchestshop.*")
    @Permission("spygchestshop.admin.*")
    public void onList(@Context Player player, @Async @Arg OfflinePlayer target, @OptionalArg Integer page) {
        if (page == null) {
            page = 1;
        }
        final int currentPage = page;
        DataManager dataManager = plugin.getDataManager();
        dataManager.getPlayerShops(target.getUniqueId()).thenAccept(shops -> {
            if (shops == null || shops.isEmpty()) {
                Message.SHOP_NO_SHOPS.send(player);
                return;
            }
            Message.SHOP_LIST_HEAD.send(player);
            List<Shop> limitedShops = shops.stream().skip((currentPage - 1) * 10).limit(10).toList();
            for (Shop shop : limitedShops) {
                Component hoverMessage = ComponentUtils.replaceComponent(Message.SHOP_LIST_SHOPS_HOVER.get(), Map.of(
                        "%shop-name%", shop.getName(),
                        "%item%", shop.getItemName(),
                        "%sell-price%", FormatUtils.formatNumber(shop.getCustomerPurchasePrice()),
                        "%buy-price%", FormatUtils.formatNumber(shop.getCustomerSalePrice()),
                        "%items-left%", shop.getItemsLeft() + "",
                        "%location%", shop.getChestLocationString(),
                        "%created%", shop.getCreatedAt()));
                player.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_LIST_SHOPS.get(), "%shop-name%", shop.getName()).hoverEvent(HoverEvent.showText(hoverMessage)));
            }
            player.sendMessage(PageUtil.getPages(currentPage, shops.size(), 10, "/spygchestshop list"));
        });
    }
}
