package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class GuiConfig extends YamlManager {

    public GuiConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        set("players.title", "&8[&6%shop-name%&8] ");
        set("shop.title", "&8[&6%shop-name%&8] &7» &8%player-name%");
        set("shop.filleritem.name", "-");
        set("shop.filleritem.material", "GRAY_STAINED_GLASS_PANE");
        set("shop.filleritem.lore", Arrays.asList("&r"));

        set("shop.material.title", "&cMaterial");
        set("shop.material.lore", Arrays.asList("&7Put here the material you want to sell in the chest"));

        set("shop.money.title", "&6Price");
        set("shop.money.lore", Arrays.asList("&7Edit the price you want to sell the material for"));

        set("shop.amount.title", "&cAmount");
        set("shop.amount.lore", Arrays.asList("&7Edit the amount of items you want to sell"));

        set("shop.player.title", "&9Added players");
        set("shop.player.lore", Arrays.asList("&7View the players that have access to this shop"));

        set("shop.info.title", "&6&lShop Info");
        set("shop.info.lore", Arrays.asList("&7owner: &e%player-name%", "&7material: &e%material%", "&7sell amount: &e%amount%", "&7price: &7$%price%", "&7created: &e%created%",
                "&7location: &e%location%", "&7sold items: &e%sold-items%", "&7money earnd: &6$&e%money-earnd%"));

        set("shop.notify.title", "&e&lNotifications");
        set("shop.notify.on", "&2ON");
        set("shop.notify.off", "&4OFF");

        saveConfig();
    }

}
