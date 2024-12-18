package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);

        set("prefix", "&8[&#ffbc1fChest&#ff711fShop&8] &7»&f");
        set("messages.config-reloaded", "%prefix% &aConfig reloaded successfully!");
        set("messages.player-only", "&cOnly players can use this command!");
        set("messages.no-permission", "%prefix% &cYou don't have permission to do that!");

        set("messages.shop.invalid-price", "%prefix% &cInvalid price!");
        set("messages.shop.no-chest", "%prefix% &cYou need to look at a chest to create a shop!");
        set("messages.shop.chest-face-not-free", "%prefix% &cThe face of the chest is not free!");
        set("messages.shop.disabled-world", "%prefix% &cShops are disabled in this world!");
        set("messages.shop.created", "%prefix% &aShop &f%shop-name% &acreated!");
        set("messages.shop.removed", "%prefix% &cShop &f%shop-name% &cremoved!");
        set("messages.shop.not-found", "%prefix% &cShop not found!");
        set("messages.shop.already-exists", "%prefix% &cShop with name &f%shop-name% &calready exists!");
        set("messages.shop.not-owner", "%prefix% &cYou are not the owner of this shop!");
        set("messages.shop.no-permission", "%prefix% &cYou don't have permission to use this shop!");
        set("messages.shop.chest-already-shop", "%prefix% &cThis chest is already a shop!");
        set("messages.shop.limit-reached", "%prefix% &cYou have reached the shop limit! (&e%shop-limit%&c)");

        set("shops.disabled-worlds", Arrays.asList("disabled_world"));
        set("shops.unknown-material", "-", Arrays.asList("The material text to use when the material is unknown."));
        set("shops.max-shops", 0, Arrays.asList("The maximum amount of shops a player can have. Set to 0 for unlimited."));
        set("shops.save-interval", 60, Arrays.asList("The interval in seconds to save the shops data."));

        set("shop.sign.line.1", "&7[&aShop&7]", Arrays.asList("You can customize the sign lines here."));
        set("shop.sign.line.2", "%owner%");
        set("shop.sign.line.3", "&a$%price%&0/%amount%");
        set("shop.sign.line.4", "%material%");
        saveConfig();
    }

}
