package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;

import hu.spyg.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);

        set("prefix", "&8[&6ChestShop&8] &7»&f");
        set("messages.no-permission", "%prefix% &cYou don't have permission to do that!");
        set("messages.invalid-price", "%prefix% &cInvalid price!");
        set("messages.no-chest", "%prefix% &cYou need to look at a chest to create a shop!");
        set("messages.chest-face-not-free", "%prefix% &cThe face of the chest is not free!");
        set("messages.player-only", "&cOnly players can use this command!");

        set("shop.sign.line.1", "&7[&aShop&7]", Arrays.asList("You can customize the sign lines here."));
        set("shop.sign.line.2", "%owner%");
        set("shop.sign.line.3", "%amount% - &a$%price%");
        set("shop.sign.line.4", "%material%");
        saveConfig();
    }

}
