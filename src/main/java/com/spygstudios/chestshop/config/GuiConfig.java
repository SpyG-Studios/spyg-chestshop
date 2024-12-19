package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class GuiConfig extends YamlManager {

    public GuiConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        set("shop.title", "&8[&6%shop-name%&8] &7» &f%player-name%");
        set("shop.filleritem.name", "-");
        set("shop.filleritem.material", "GRAY_STAINED_GLASS_PANE");
        set("shop.filleritem.lore", Arrays.asList("&r"));

        saveConfig();
    }

}
