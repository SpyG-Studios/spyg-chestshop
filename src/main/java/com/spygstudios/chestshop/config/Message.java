package com.spygstudios.chestshop.config;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spygstudios.spyglib.components.ComponentUtils;

import net.kyori.adventure.text.Component;

public enum Message {
    CONFIG_RELOADED("config-reloaded", "%prefix% &aConfig reloaded successfully!"), PLAYER_ONLY("player-only", "&cOnly players can use this command!"),
    NO_PERMISSION("no-permission", "%prefix% &cYou don't have permission to do that!"), SHOP_INVALID_PRICE("shop.invalid-price", "%prefix% &cInvalid price!"),
    SHOP_NO_CHEST("shop.no-chest", "%prefix% &cYou need to look at a chest to create a shop!"), SHOP_CHEST_FACE_NOT_FREE("shop.chest-face-not-free", "%prefix% &cThe face of the chest is not free!"),
    SHOP_DISABLED_WORLD("shop.disabled-world", "%prefix% &cShops are disabled in this world!"), SHOP_CREATED("shop.created", "%prefix% &aShop &f%shop-name% &acreated!"),
    SHOP_REMOVED("shop.removed", "%prefix% &cShop &f%shop-name% &cremoved!"), SHOP_NOT_FOUND("shop.not-found", "%prefix% &cShop not found!"),
    SHOP_ALREADY_EXISTS("shop.already-exists", "%prefix% &cShop with name &f%shop-name% &calready exists!"), SHOP_NOT_OWNER("shop.not-owner", "%prefix% &cYou are not the owner of this shop!"),
    SHOP_NO_PERMISSION("shop.no-permission", "%prefix% &cYou don't have permission to use this shop!"), SHOP_CHEST_ALREADY_SHOP("shop.chest-already-shop", "%prefix% &cThis chest is already a shop!"),
    SHOP_LIMIT_REACHED("shop.limit-reached", "%prefix% &cYou have reached the shop limit! (&e%shop-limit%&c)"), SHOP_SETUP_NEEDED("shop.setup-needed", "%prefix% &cThis shop is not set up properly!"),
    SHOP_BOUGHT("shop.bought", "%prefix% &aYou bought &f%material% &afor &f$%price%&a!"), SHOP_SOLD("shop.sold", "%prefix% &aYou sold &f%material% &afor &f$%price%&a, to %player-name%!"),
    NOT_ENOUGH_MONEY("not-enough-money", "%prefix% &cYou don't have enough money! ($%price%)");

    private String node;
    private String defaultMessage;
    private static Config config;

    private Message(String node, String defaultMessage) {
        this.node = node;
        this.defaultMessage = defaultMessage;
    }

    public void sendMessage(Player player) {
        player.sendMessage(get());
    }

    public void sendMessage(CommandSender sender) {
        sender.sendMessage(get());
    }

    public void sendMessage(Player player, Map<String, String> placeholders) {
        player.sendMessage(ComponentUtils.replaceComponent(get(), placeholders));
    }

    public void sendMessage(CommandSender sender, Map<String, String> placeholders) {
        sender.sendMessage(ComponentUtils.replaceComponent(get(), placeholders));
    }

    public String getNode() {
        return node;
    }

    public String getRaw() {
        return config.getString("messages." + node);
    }

    public Component get() {
        return config.getMessage(node);
    }

    public void setDefault() {
        if (config.getString("messages." + node) == null) {
            config.set("messages." + node, defaultMessage);
        }
        config.saveConfig();
    }

    public static void init(Config conf) {
        config = conf;
        for (Message message : Message.values()) {
            message.setDefault();
        }
    }
}
