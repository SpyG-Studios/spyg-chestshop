package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class GuiConfig extends YamlManager {

    public GuiConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        set("chestshop.title", "&c%shop-name% &8(%player-name%)");

        set("chestshop.material.title", "&cMaterial");
        set("chestshop.material.lore", Arrays.asList("&7Put here the material you", "&7want to sell"));

        set("chestshop.money.title", "&6Price");
        set("chestshop.money.material", "GOLD_INGOT");
        set("chestshop.money.lore", Arrays.asList("&7Edit the price you want to", "&7sell the material for"));

        set("chestshop.inventory.title", "&cShopInventory");
        set("chestshop.inventory.material", "CHEST");
        set("chestshop.inventory.lore", Arrays.asList("&7Click to open the shop inventory"));

        set("chestshop.player.title", "&9Added players");
        set("chestshop.player.lore", Arrays.asList("&7View the players that have access to this shop"));

        set("chestshop.info.title", "&6&lShop Info");
        set("chestshop.info.material", "WRITABLE_BOOK");
        set("chestshop.info.lore", Arrays.asList("&7owner: &e%player-name%", "&7material: &e%material%", "&7price: &6$&e%price%", "&7money earned: &6$&e%money-earned%", "&7sold items: &e%sold-items%",
                "&7location: &e%location%", "&7created: &e%created%"));

        set("chestshop.notify.title", "&e&lBuy Notifications");
        set("chestshop.notify.material", "BELL");
        set("chestshop.notify.on", "&2ON");
        set("chestshop.notify.off", "&4OFF");
        set("chestshop.fill-items.filler1.material", "GRAY_STAINED_GLASS_PANE");
        set("chestshop.fill-items.filler1.slots", Arrays.asList());

        set("players.title", "&9Added players");
        set("players.player.title", "&6&l%player-name%");
        set("players.player.lore", Arrays.asList("&7Click to remove this player"));
        set("players.next.title", "&6Next page");
        set("players.next.material", "ARROW");
        set("players.back.title", "&6Previous page");
        set("players.back.material", "ARROW");
        set("players.fill-items.filler1.material", "GRAY_STAINED_GLASS_PANE");
        set("players.fill-items.filler1.slots",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"));

        set("shop.title", "&c%shop-name%");
        set("shop.item-to-buy.title", "&6&lBuy &7%material%");
        set("shop.item-to-buy.lore", Arrays.asList("&7Buy item for &6$&e%price%"));
        set("shop.fill-items.filler1.material", "GRAY_STAINED_GLASS_PANE");
        set("shop.fill-items.filler1.slots", Arrays.asList());

        set("shop.amount.items.minus1.slot", 9);
        set("shop.amount.items.minus1.title", "-&c%amount%");
        set("shop.amount.items.minus1.material", "RED_STAINED_GLASS_PANE");
        set("shop.amount.items.minus1.lore", Arrays.asList("&7Click to decrease the amount"));
        set("shop.amount.items.minus1.amount", -16);

        set("shop.amount.items.minus2.slot", 11);
        set("shop.amount.items.minus2.title", "-&c%amount%");
        set("shop.amount.items.minus2.material", "RED_STAINED_GLASS_PANE");
        set("shop.amount.items.minus2.lore", Arrays.asList("&7Click to decrease the amount"));
        set("shop.amount.items.minus2.amount", -1);

        set("shop.amount.items.plus1.slot", 15);
        set("shop.amount.items.plus1.title", "+&a%amount%");
        set("shop.amount.items.plus1.material", "LIME_STAINED_GLASS_PANE");
        set("shop.amount.items.plus1.lore", Arrays.asList("&7Click to increase the amount"));
        set("shop.amount.items.plus1.amount", 1);

        set("shop.amount.items.plus2.slot", 17);
        set("shop.amount.items.plus2.title", "+&a%amount%");
        set("shop.amount.items.plus2.material", "LIME_STAINED_GLASS_PANE");
        set("shop.amount.items.plus2.lore", Arrays.asList("&7Click to increase the amount"));
        set("shop.amount.items.plus2.amount", 16);

        saveConfig();
    }

}
