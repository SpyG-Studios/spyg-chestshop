package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class MenuConfig extends YamlManager {

    public MenuConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        setOrDefault("chestshop.title", "&c%shop-name% &8(%player-name%)");

        setOrDefault("chestshop.item.title", "&cItem to Sell");
        setOrDefault("chestshop.item.slot", 13);
        setOrDefault("chestshop.item.lore", Arrays.asList("&7Put here the item you", "&7want to sell"));
        setOrDefault("chestshop.item.not-set", "BARRIER");

        setOrDefault("chestshop.money.title", "&6&lPrice Settings");
        setOrDefault("chestshop.money.slot", 11);
        setOrDefault("chestshop.money.material", "GOLD_INGOT");
        setOrDefault("chestshop.money.lore", Arrays.asList("&7Sell Price: &a$%sell-price%", "&7Buy Price: &c$%buy-price%", "", "&6Left click: &7Set item &asell &7price (player buys)",
                "&6Right click: &7Set item &cbuy &7price (player sells)"));

        setOrDefault("chestshop.inventory.title", "&cShopInventory");
        setOrDefault("chestshop.inventory.slot", 18);
        setOrDefault("chestshop.inventory.material", "CHEST");
        setOrDefault("chestshop.inventory.lore", Arrays.asList("&7Click to open the shop inventory"));

        setOrDefault("chestshop.player.title", "&9Added players");
        setOrDefault("chestshop.player.slot", 26);
        setOrDefault("chestshop.player.lore", Arrays.asList("&7View the players that have access to this shop"));

        setOrDefault("chestshop.info.title", "&6&lShop Info");
        setOrDefault("chestshop.info.slot", 8);
        setOrDefault("chestshop.info.material", "WRITABLE_BOOK");
        setOrDefault("chestshop.info.lore",
                Arrays.asList(
                        "&7owner: &e%player-name%",
                        "&7item: &e%item%",
                        "&7price: &e%price%",
                        "&7sold items: &e%sold-items%",
                        "&7money earned: &6$&e%money-earned%",
                        "&7bought items: &e%bought-items%",
                        "&7money spent: &6$&e%money-spent%",
                        "&7location: &e%location%",
                        "&7created: &e%created%"));

        setOrDefault("chestshop.notify.title", "&eBuy/Sell Notifications");
        setOrDefault("chestshop.notify.material", "BELL");
        setOrDefault("chestshop.notify.on", "&2ON");
        setOrDefault("chestshop.notify.off", "&4OFF");

        setOrDefault("chestshop.buysell.title", "&6&lSell mode Toggle");
        setOrDefault("chestshop.buysell.slot", 16);
        setOrDefault("chestshop.buysell.material", "LEVER");
        setOrDefault("chestshop.buysell.sell.line", "&7Selling: %status%");
        setOrDefault("chestshop.buysell.buy.line", "&7Buying: %status%");
        setOrDefault("chestshop.buysell.sell.enabled", "&a&lENABLED");
        setOrDefault("chestshop.buysell.sell.disabled", "&c&lDISABLED");
        setOrDefault("chestshop.buysell.buy.enabled", "&a&lENABLED");
        setOrDefault("chestshop.buysell.buy.disabled", "&c&lDISABLED");
        setOrDefault("chestshop.buysell.lore", Arrays.asList("", "&7Click to toggle sell mode"));

        setOrDefault("chestshop.quantity.title", "&b&lQuantity");
        setOrDefault("chestshop.quantity.slot", 15);
        setOrDefault("chestshop.quantity.material", "HOPPER");
        setOrDefault("chestshop.quantity.lore", Arrays.asList("&7Items per transaction: &e%quantity%", "", "&7Click to change"));

        setOrDefault("chestshop.fill-items.filler1.material", "GRAY_STAINED_GLASS_PANE");
        setOrDefault("chestshop.fill-items.filler1.slots", Arrays.asList());

        setOrDefault("players.title", "&9Added players");
        setOrDefault("players.player.title", "&6&l%player-name%");
        setOrDefault("players.player.lore", Arrays.asList("&7Click to remove this player"));
        setOrDefault("players.next.title", "&6Next page");
        setOrDefault("players.next.material", "ARROW");
        setOrDefault("players.back.title", "&6Previous page");
        setOrDefault("players.back.material", "ARROW");
        setOrDefault("players.fill-items.filler1.material", "GRAY_STAINED_GLASS_PANE");
        setOrDefault("players.fill-items.filler1.slots",
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"));

        setOrDefault("shop.title", "&c%shop-name%");
        setOrDefault("shop.item-to-buy.title", "&6&lBuy &7%item%");
        setOrDefault("shop.item-to-buy.lore", Arrays.asList("&7Buy item for &6$&e%price%"));
        setOrDefault("shop.item-to-sell.title", "&6&lSell &7%item%");
        setOrDefault("shop.item-to-sell.lore", Arrays.asList("&7Sell item for &6$&e%price%"));
        setOrDefault("shop.mode.buying.title", "&a&lBuying Mode");
        setOrDefault("shop.mode.buying.material", "EMERALD");
        setOrDefault("shop.mode.buying.lore", Arrays.asList("&7Click to switch to selling mode"));
        setOrDefault("shop.mode.selling.title", "&c&lSelling Mode");
        setOrDefault("shop.mode.selling.material", "REDSTONE");
        setOrDefault("shop.mode.selling.lore", Arrays.asList("&7Click to switch to buying mode"));
        setOrDefault("shop.fill-items.filler1.material", "GRAY_STAINED_GLASS_PANE");
        setOrDefault("shop.fill-items.filler1.slots", Arrays.asList());

        setOrDefault("shop.amount.items.minus1.slot", 9);
        setOrDefault("shop.amount.items.minus1.title", "-&c%amount%");
        setOrDefault("shop.amount.items.minus1.material", "RED_STAINED_GLASS_PANE");
        setOrDefault("shop.amount.items.minus1.lore", Arrays.asList("&7Click to decrease the amount"));
        setOrDefault("shop.amount.items.minus1.amount", -16);

        setOrDefault("shop.amount.items.minus2.slot", 11);
        setOrDefault("shop.amount.items.minus2.title", "-&c%amount%");
        setOrDefault("shop.amount.items.minus2.material", "RED_STAINED_GLASS_PANE");
        setOrDefault("shop.amount.items.minus2.lore", Arrays.asList("&7Click to decrease the amount"));
        setOrDefault("shop.amount.items.minus2.amount", -1);

        setOrDefault("shop.amount.items.plus1.slot", 15);
        setOrDefault("shop.amount.items.plus1.title", "+&a%amount%");
        setOrDefault("shop.amount.items.plus1.material", "LIME_STAINED_GLASS_PANE");
        setOrDefault("shop.amount.items.plus1.lore", Arrays.asList("&7Click to increase the amount"));
        setOrDefault("shop.amount.items.plus1.amount", 1);

        setOrDefault("shop.amount.items.plus2.slot", 17);
        setOrDefault("shop.amount.items.plus2.title", "+&a%amount%");
        setOrDefault("shop.amount.items.plus2.material", "LIME_STAINED_GLASS_PANE");
        setOrDefault("shop.amount.items.plus2.lore", Arrays.asList("&7Click to increase the amount"));
        setOrDefault("shop.amount.items.plus2.amount", 16);

        saveConfig();
    }

}
