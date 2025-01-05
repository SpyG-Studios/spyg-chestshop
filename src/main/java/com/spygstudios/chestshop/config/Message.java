package com.spygstudios.chestshop.config;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spygstudios.spyglib.components.ComponentUtils;

import net.kyori.adventure.text.Component;

public enum Message {
    COMMANDS("commands", "%prefix% &7Commands:"),

    ENTER_AMOUNT_CANCELLED("enter-amount-cancelled", "%prefix% &cAmount entering cancelled!"),
    ENTER_AMOUNT("enter-amount-prompt", "%prefix% &aEnter the amount &7&o(Enter a &f%cancel%&7&o to cancel)"),
    ENTER_AMOUNT_SUCCESS("enter-amount-success", "%prefix% &aAmount set successfully!"),
    
    INVALID_NUMBER("invalid-number", "%prefix% &cInvalid number! (&e%entered%&c)"),

    CONFIG_RELOADED("config-reloaded", "%prefix% &aConfig reloaded successfully!"), 
    NO_PERMISSION("no-permission", "%prefix% &cYou don't have permission to do that!"), 
    NOT_ENOUGH_MONEY("not-enough-money", "%prefix% &cYou don't have enough money! ($%price%)"), 
    PLAYER_ONLY("player-only", "&cOnly players can use this command!"),
    SHOP_ALREADY_EXISTS("shop.already-exists", "%prefix% &cShop with name &f%shop-name% &calready exists!"), 
    SHOP_BOUGHT("shop.bought", "%prefix% &aYou bought &f%material% &afor &f$%price%&a! &7(&8%items-left%&7)"), 
    SHOP_CHEST_ALREADY_SHOP("shop.chest-already-shop", "%prefix% &cThis chest is already a shop!"),
    SHOP_CHEST_FACE_NOT_FREE("shop.chest-face-not-free", "%prefix% &cThe face of the chest is not free!"),
    SHOP_CREATED("shop.created", "%prefix% &aShop &f%shop-name% &acreated!"),
    SHOP_DISABLED_WORLD("shop.disabled-world", "%prefix% &cShops are disabled in this world!"), 
    SHOP_INVALID_PRICE("shop.invalid-price", "%prefix% &cInvalid price!"),
    SHOP_INVALID_PAGE("shop.invalid-page", "%prefix% &cInvalid page! &7(&8%page%&7)"),
    SHOP_LIMIT_REACHED("shop.limit-reached", "%prefix% &cYou have reached the shop limit! (&e%shop-limit%&c)"), 
    
    SHOP_LIST_HEAD("shop.list.header", "%prefix% &aYour shops:"), 
    SHOP_LIST_SHOPS("shop.list.shops", "&7- &f%shop-name%"),
    SHOP_LIST_SHOPS_HOVER("shop.list.shops-hover", "&6&l%material%&r\n &8- &7price: &6$&e%price%\n &8- &7amount: &e%amount%\n &8- &7items left: &e%items-left%\n &8- &7location: &e%location%\n &8- &7created at: &e%created%"),
    SHOP_LIST_CURRENT_PAGE("shop.list.current-page", "&7[&e&l%page%&r&7]"),
    SHOP_LIST_NEXT("shop.list.next", " &7[&eNext&7]"),
    SHOP_LIST_BACK("shop.list.back", "&7[&cBack&7]"),
    SHOP_LIST_PAGE("shop.list.page", "&7[&e%page%&7]"),

    SHOP_NO_CHEST("shop.no-chest", "%prefix% &cYou need to look at a chest to create a shop!"), 
    SHOP_EMPTY("shop.empty", "%prefix% &cThe shop is empty!"),
    SHOP_INVENTORY_FULL("shop.inventory-full", "%prefix% &cYour inventory is full!"),
    SHOP_EXPLODED("shop.exploded", "%prefix% &cYour %shop-name% shop exploded! &7&o(&f%shop-location%&7)"),
    SHOP_NO_PERMISSION("shop.no-permission", "%prefix% &cYou don't have permission to use this shop!"), 
    SHOP_NO_SHOPS("shop.no-shops", "%prefix% &cYou don't have any shops!"),
    SHOP_NOT_FOUND("shop.not-found", "%prefix% &cShop not found!"),
    SHOP_NOT_OWNER("shop.not-owner", "%prefix% &cYou are not the owner of this shop!"),
    SHOP_REMOVED("shop.removed", "%prefix% &cShop &f%shop-name% &cremoved!"), 
    SHOP_SETUP_NEEDED("shop.setup-needed", "%prefix% &cThis shop is not set up properly!"),
    SHOP_NAME_TOO_SHORT("shop.name-too-short", "%prefix% &cShop name is too short!"),
    SHOP_SOLD("shop.sold", "%prefix% &aYou sold &f%material% &afor &f$%price%&a, to %player-name%! &7(&8%items-left%&7)");

    private String node;
    private String defaultMessage;
    private static Config config;

    private Message(String node, String defaultMessage) {
        this.node = node;
        this.defaultMessage = defaultMessage;
    }

    public void send(Player player) {
        player.sendMessage(get());
    }

    public void send(CommandSender sender) {
        sender.sendMessage(get());
    }

    public void send(Player player, Map<String, String> placeholders) {
        player.sendMessage(ComponentUtils.replaceComponent(get(), placeholders));
    }

    public void send(CommandSender sender, Map<String, String> placeholders) {
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
