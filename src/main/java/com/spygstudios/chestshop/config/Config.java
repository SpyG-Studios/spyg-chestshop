package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);

        set("prefix", "&8[&#ffbc1fChest&#ff711fShop&8] &7»&f");
        set("cancel", "cancel");

        set("colors.command.required-arg", " &c");
        set("colors.command.optional-arg", " &7");
        set("colors.command.list", "&8- ");
        set("colors.command.label", "&7");
        set("colors.command.args", "&f");

        set("shops.disabled-worlds", Arrays.asList("disabled_world"));
        set("shops.unknown-material", "-", Arrays.asList("The material text to use when the material is unknown."));
        set("shops.max-shops", 0, Arrays.asList("The maximum amount of shops a player can have. Set to 0 for unlimited."));
        set("shops.max-players", 0, Arrays.asList("The maximum amount of players that can be added to a shop. Set to 0 for unlimited."));
        set("shops.save-interval", 60, Arrays.asList("The interval in seconds to save the shops data."));

        set("shop.anti-explosion", true, Arrays.asList("Prevent shops from being destroyed by explosions."));
        set("shop.triggers", Arrays.asList("[chestshop]", "[cs]", "[shop]"), Arrays.asList("The sign text that triggers a shop."));
        set("shop.sign.line.1", "&0[&b%material%&0]", Arrays.asList("You can customize the sign lines here."));
        set("shop.sign.line.2", "%owner%");
        set("shop.sign.line.3", "x&c%amount%");
        set("shop.sign.line.4", "&a$&f%price%");
        saveConfig();
    }

}
