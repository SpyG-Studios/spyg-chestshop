package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);

        set("prefix", "&8[&#ffbc1fChest&#ff711fShop&8] &7»&f");

        set("shops.disabled-worlds", Arrays.asList("disabled_world"));
        set("shops.unknown-material", "-", Arrays.asList("The material text to use when the material is unknown."));
        set("shops.max-shops", 0, Arrays.asList("The maximum amount of shops a player can have. Set to 0 for unlimited."));
        set("shops.save-interval", 60, Arrays.asList("The interval in seconds to save the shops data."));

        set("shop.sign.line.1", "&7[&aShop&7]", Arrays.asList("You can customize the sign lines here."));
        set("shop.sign.line.2", "%owner%");
        set("shop.sign.line.3", "&#1a652a$&0%price%/%amount%");
        set("shop.sign.line.4", "%material%");
        saveConfig();
    }

}
