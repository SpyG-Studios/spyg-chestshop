package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);
        set("check-for-updates", true, Arrays.asList("Check for updates on join (spygchestshop.admin.updates)."));
        set("locale", "en_US", Arrays.asList("The locale of the plugin. (file name)"));
        set("cancel", "cancel", Arrays.asList("The text to cancel the amount action."));
        set("colors.command.required-arg", " &c");
        set("colors.command.optional-arg", " &7");
        set("colors.command.list", "&8- ");
        set("colors.command.label", "&7");
        set("colors.command.args", "&f");

        set("shops.name.max-length", 16);
        set("shops.name.min-length", 3);
        set("shops.blacklisted-names", Arrays.asList("stupid"));
        set("shops.disabled-worlds", Arrays.asList("disabled_world"));
        set("shops.unknown-material", "-", Arrays.asList("The material text to use when the material is unknown."));
        set("shops.max-shops.default", -1,
                Arrays.asList("The maximum amount of shops a player can have (spygchestshop.max.<group>). Set to -1 for unlimited. If a player has more than one permission, the highest amount will be used."));
        set("shops.max-players", 18, Arrays.asList("The maximum amount of players that can be added to a shop. Set to 0 for unlimited."));
        set("shops.save-interval", 60, Arrays.asList("The interval in seconds to save the shops data."));

        set("shop.anti-explosion", true, Arrays.asList("Prevent shops from being destroyed by explosions."));
        set("shop.hopper-protection", true,
                Arrays.asList("Prevent hoppers from taking items from shops. !!!YOU MUST TURN OFF \"hopper.disable-move-event\" IN PAPER CONFIG IN ORDER TO PROTECT THE HOPPERS!!!"));
        set("shop.lines", Arrays.asList("%shop-name%", "&#dddddd%owner%", "&2$&a%price%"), Arrays.asList("The lines of the shop."));
        saveConfig();
    }

}
