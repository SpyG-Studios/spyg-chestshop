package com.spygstudios.chestshop.commands;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;
import com.spygstudios.spyglib.components.ComponentUtils;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent;

@Command(name = "spygchestshop list", aliases = { "spcs list", "chestshop list", "scs list" })
public class ShopList {

    @Execute
    public void onList(@Context Player player, @OptionalArg Integer page) {
        if (page == null) {
            page = 1;
        }

        ShopFile file = ShopFile.getShopFile(player);
        if (file == null || file.getPlayerShops().isEmpty()) {
            Message.SHOP_NO_SHOPS.sendMessage(player);
            return;
        }

        int pages = (int) Math.ceil((double) file.getPlayerShops().size() / 10);
        if (pages < page || page < 1) {
            player.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_INVALID_PAGE.get(), Map.of("%page%", page + "")));
            return;
        }

        Message.SHOP_LIST_HEAD.sendMessage(player);
        List<Shop> shops = Shop.getShops(player).stream().sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).skip((page - 1) * 10).limit(10).collect(Collectors.toList());
        for (Shop shop : shops) {
            Chest chest = (Chest) shop.getChestLocation().getBlock().getState();

            String itemsLeft = String.valueOf(InventoryUtils.countItems(chest.getInventory(), shop.getMaterial()));
            Component hoverMessage = ComponentUtils.replaceComponent(Message.SHOP_LIST_SHOPS_HOVER.get(), Map.of("%shop-name%", shop.getName(), "%material%", shop.getMaterialString(), "%price%",
                    shop.getPrice() + "", "%amount%", shop.getAmount() + "", "%items-left%", itemsLeft, "%location%", shop.getChestLocationString(), "%created%", shop.getCreatedAt()));
            player.sendMessage(ComponentUtils.replaceComponent(Message.SHOP_LIST_SHOPS.get(), "%shop-name%", shop.getName()).hoverEvent(HoverEvent.showText(hoverMessage)));
        }

        Component back = Message.SHOP_LIST_BACK.get().clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/spygchestshop list " + (page - 1)));
        Component next = Message.SHOP_LIST_NEXT.get().clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/spygchestshop list " + (page + 1)));
        Builder pagesComponent = Component.text();
        for (int i = 0; i < pages; i++) {
            if (i + 1 == page) {
                pagesComponent.append(ComponentUtils.replaceComponent(Message.SHOP_LIST_CURRENT_PAGE.get(), "%page%", String.valueOf(i + 1)));
                continue;
            }
            pagesComponent.append(ComponentUtils.replaceComponent(Message.SHOP_LIST_PAGE.get(), "%page%", String.valueOf(i + 1)))
                    .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/spygchestshop list " + (i + 1)));
        }

        Component message;
        if (pages == 1) {
            message = pagesComponent.build(); // Csak egy oldal van
        } else if (page < pages && page == 1) {
            message = Component.text().append(pagesComponent.build()).append(next).build(); // Van még oldal, de az elsőn vagyunk
        } else if (page < pages) {
            message = Component.text().append(back).append(pagesComponent.build()).append(next).build(); // Van még oldal, de nem az elsőn vagyunk
        } else {
            message = Component.text().append(back).append(pagesComponent.build()).build(); // Az utolsó oldalon vagyunk
        }

        player.sendMessage(message);
    }
}
