package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class GuiConfig extends YamlManager {

    public GuiConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        set("chestshop.title", "&8[&6%shop-name%&8] &7 &8%player-name%");

        set("chestshop.material.title", "&cMaterial");
        set("chestshop.material.lore", Arrays.asList("&7Put here the material you want to sell in the chest"));

        set("chestshop.money.title", "&6Price");
        set("chestshop.money.lore", Arrays.asList("&7Edit the price you want to sell the material for"));

        set("chestshop.inventory.title", "&cShopInventory");
        set("chestshop.inventory.lore", Arrays.asList("&7Click to open the shop inventory"));

        set("chestshop.player.title", "&9Added players");
        set("chestshop.player.lore", Arrays.asList("&7View the players that have access to this shop"));

        set("chestshop.info.title", "&6&lShop Info");
        set("chestshop.info.lore", Arrays.asList("&7owner: &e%player-name%", "&7material: &e%material%", "&7price: &6$&e%price%", "&7money earnd: &6$&e%money-earnd%", "&7sold items: &e%sold-items%",
                "&7location: &e%location%", "&7created: &e%created%"));

        set("chestshop.notify.title", "&e&lBuy Notifications");
        set("chestshop.notify.on", "&2ON");
        set("chestshop.notify.off", "&4OFF");

        set("players.title", "&8[&6%shop-name%&8] &9Added players");
        set("players.player.title", "&6&l%player-name%");
        set("players.player.lore", Arrays.asList("&7Click to remove this player"));

        set("shop.title", "&8[&6%shop-name%&8] &7Shop");
        set("shop.minus.title", "&c%amount%");
        set("shop.minus.lore", Arrays.asList("&7Click to decrease the amount"));
        set("shop.plus.title", "&a+%amount%");
        set("shop.plus.lore", Arrays.asList("&7Click to increase the amount"));
        set("shop.item-to-buy.title", "&6&lBuy &7%material%");
        set("shop.item-to-buy.lore", Arrays.asList("&7Buy item for &6$&e%price%"));

        set("shop.amount.items.1", -32);
        set("shop.amount.items.2", -1);
        set("shop.amount.items.3", 1);
        set("shop.amount.items.4", 32);

        saveConfig();
    }

}
