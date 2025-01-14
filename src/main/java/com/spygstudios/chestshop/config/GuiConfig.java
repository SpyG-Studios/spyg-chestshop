package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class GuiConfig extends YamlManager {

    public GuiConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        set("shop.title", "&8[&6%shop-name%&8] &7 &8%player-name%");

        set("shop.material.title", "&cMaterial");
        set("shop.material.lore", Arrays.asList("&7Put here the material you want to sell in the chest"));

        set("shop.money.title", "&6Price");
        set("shop.money.lore", Arrays.asList("&7Edit the price you want to sell the material for"));

        set("shop.inventory.title", "&cShopInventory");
        set("shop.inventory.lore", Arrays.asList("&7Click to open the shop inventory"));

        set("players.title", "&8[&6%shop-name%&8] &9Added players");
        set("players.player.title", "&6&l%player-name%");
        set("players.player.lore", Arrays.asList("&7Click to remove this player"));

        set("shop.player.title", "&9Added players");
        set("shop.player.lore", Arrays.asList("&7View the players that have access to this shop"));

        set("shop.info.title", "&6&lShop Info");
        set("shop.info.lore", Arrays.asList("&7owner: &e%player-name%", "&7material: &e%material%", "&7price: &6$&e%price%", "&7money earnd: &6$&e%money-earnd%", "&7sold items: &e%sold-items%",
                "&7location: &e%location%", "&7created: &e%created%"));

        set("shop.notify.title", "&e&lBuy Notifications");
        set("shop.notify.on", "&2ON");
        set("shop.notify.off", "&4OFF");

        saveConfig();
    }

}
