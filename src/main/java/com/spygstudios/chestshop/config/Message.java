package com.spygstudios.chestshop.config;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spygstudios.spyglib.components.ComponentUtils;

import net.kyori.adventure.text.Component;

public enum Message {
    ENTER_AMOUNT_CANCELLED("enter-amount-cancelled", "%prefix% &cAmount entering cancelled!"),
    ENTER_AMOUNT_INVALID("enter-amount-invalid", "%prefix% &cInvalid amount! (&e%entered%&c)"),
    ENTER_AMOUNT("enter-amount-prompt", "%prefix% &aEnter the amount &7&o(Enter a &f%cancel%&7&o to cancel)::"),

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
    SHOP_LIST_SHOPS("shop.list.shops", "&7- &f%shop-name% &7(&f%material%&7) &7- &#1a652a$&f%price%&7/&f%amount% &7items left: &8%items-left%"),
    SHOP_LIST_CURRENT_PAGE("shop.list.current-page", "&7[&e&l%page%&r&7]"),
    SHOP_LIST_NEXT("shop.list.next", " &7[&eNext&7]"),
    SHOP_LIST_BACK("shop.list.back", "&7[&cBack&7]"),
    SHOP_LIST_PAGE("shop.list.page", "&7[&e%page%&7]"),

    SHOP_NO_CHEST("shop.no-chest", "%prefix% &cYou need to look at a chest to create a shop!"), 
    SHOP_EMPTY("shop.empty", "%prefix% &cThe shop is empty!"),
    SHOP_NO_PERMISSION("shop.no-permission", "%prefix% &cYou don't have permission to use this shop!"), 
    SHOP_NO_SHOPS("shop.no-shops", "%prefix% &cYou don't have any shops!"),
    SHOP_NOT_FOUND("shop.not-found", "%prefix% &cShop not found!"),
    SHOP_NOT_OWNER("shop.not-owner", "%prefix% &cYou are not the owner of this shop!"),
    SHOP_REMOVED("shop.removed", "%prefix% &cShop &f%shop-name% &cremoved!"), 
    SHOP_SETUP_NEEDED("shop.setup-needed", "%prefix% &cThis shop is not set up properly!"),
    SHOP_SOLD("shop.sold", "%prefix% &aYou sold &f%material% &afor &f$%price%&a, to %player-name%! &7(&8%items-left%&7)"),;

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
