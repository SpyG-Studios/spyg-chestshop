package com.spygstudios.chestshop;

import java.util.Map;

import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.components.ComponentUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;

public class PageUtil {

    public static Component getPages(int page, int maxPage, String command) {
        int pages = (int) Math.ceil((double) maxPage / 10);
        if (pages < page || page < 1) {
            return ComponentUtils.replaceComponent(Message.SHOP_INVALID_PAGE.get(), Map.of("%page%", page + ""));
        }

        Component back = Message.SHOP_LIST_BACK.get().clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, command + " " + (page - 1)));
        Component next = Message.SHOP_LIST_NEXT.get().clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, command + " " + (page + 1)));
        Builder pagesComponent = Component.text();
        for (int i = 0; i < pages; i++) {
            if (i + 1 == page) {
                pagesComponent.append(ComponentUtils.replaceComponent(Message.SHOP_LIST_CURRENT_PAGE.get(), "%page%", String.valueOf(i + 1)));
                continue;
            }
            pagesComponent.append(ComponentUtils.replaceComponent(Message.SHOP_LIST_PAGE.get(), "%page%", String.valueOf(i + 1)))
                    .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, command + " " + (i + 1)));
        }

        if (pages == 1) {
            return pagesComponent.build(); // Csak egy oldal van
        } else if (page < pages && page == 1) {
            return Component.text().append(pagesComponent.build()).append(next).build();
        } else if (page < pages) {
            return Component.text().append(back).append(pagesComponent.build()).append(next).build();
        } else {
            return Component.text().append(back).append(pagesComponent.build()).build();
        }
    }
}
