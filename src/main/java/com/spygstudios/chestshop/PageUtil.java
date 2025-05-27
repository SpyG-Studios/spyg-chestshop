package com.spygstudios.chestshop;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.components.ComponentUtils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;

@UtilityClass
public class PageUtil {

    public static Component getPages(int page, int allPages, int elementPerPage, String command) {
        int pages = (int) Math.ceil((double) allPages / elementPerPage);
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

    public static void setFillItems(Inventory inventory, String configPath) {
        GuiConfig config = ChestShop.getInstance().getGuiConfig();
        config.getConfigurationSection(configPath + ".fill-items").getKeys(false).forEach(key -> {
            String itemPath = configPath + ".fill-items." + key;
            Material material = Material.getMaterial(config.getString(itemPath + ".material"));
            if (material == null) {
                ChestShop.getInstance().getLogger().warning("Invalid material in " + configPath + " fill-items: " + config.getString(itemPath + ".material"));
                return;
            }
            List<String> slots = config.getStringList(itemPath + ".slots");
            ItemStack fillerItem = new ItemStack(material);
            for (String s : slots) {
                int slot = Integer.parseInt(s);
                if (slot < 0 || slot >= inventory.getSize()) {
                    ChestShop.getInstance().getLogger().warning("Invalid slot in " + configPath + " fill-items: " + s);
                    continue;
                }
                if (inventory.getItem(slot) != null) {
                    ChestShop.getInstance().getLogger().warning("Slot " + slot + " is already occupied in " + configPath + " fill-items.");
                    continue;
                }
                ItemMeta glassMeta = fillerItem.getItemMeta();
                glassMeta.displayName(TranslateColor.translate("&7"));
                fillerItem.setItemMeta(glassMeta);
                inventory.setItem(slot, fillerItem);
            }
        });

    }
}
