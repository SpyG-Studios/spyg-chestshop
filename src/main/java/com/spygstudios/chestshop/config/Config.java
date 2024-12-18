package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);

        set("prefix", "&8[&6ChestShop&8] &7»&f");
        set("messages.no-permission", "%prefix% &cYou don't have permission to do that!");
        set("messages.invalid-price", "%prefix% &cInvalid price!");
        set("messages.no-chest", "%prefix% &cYou need to look at a chest to create a shop!");
        set("messages.chest-face-not-free", "%prefix% &cThe face of the chest is not free!");
        set("messages.player-only", "&cOnly players can use this command!");

        set("messages.shop.created", "%prefix% &aShop created!");
        set("messages.shop.removed", "%prefix% &cShop removed!");
        set("messages.shop.not-found", "%prefix% &cShop not found!");
        set("messages.shop.already-exists", "%prefix% &cShop with name '%shop-name%' already exists!");

        set("shops.disabled-worlds", Arrays.asList("disabled_world"));
        set("shops.max-shops", 0, Arrays.asList("The maximum amount of shops a player can have. Set to 0 for unlimited."));

        set("shop.sign.line.1", "&7[&aShop&7]", Arrays.asList("You can customize the sign lines here."));
        set("shop.sign.line.2", "%owner%");
        set("shop.sign.line.3", "&a$%price%&0/%amount%");
        set("shop.sign.line.4", "%material%");
        saveConfig();
    }

}
