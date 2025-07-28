package com.spygstudios.chestshop.config;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.components.ComponentUtils;

import net.kyori.adventure.text.Component;

public enum Message {

    PREFIX("prefix", "&8[&#ffbc1fChest&#ff711fShop&8] &7»&f"),
    COMMANDS("commands", "%prefix% &7Commands:"),
    NEW_VERSION("new-version", "%prefix% &aA new version of SpygChestShop is available! Current version: &#e80000%old-version% &aNew version: &#00bf0d%new-version% &7&o(Click to download)"),

    ENTER_AMOUNT_CANCELLED("enter-amount-cancelled", "%prefix% &cAmount entering cancelled!"),
    ENTER_AMOUNT("enter-amount-prompt", "%prefix% &aEnter the amount &7&o(Enter a &f%cancel%&7&o to cancel)"),
    ENTER_AMOUNT_SUCCESS("enter-amount-success", "%prefix% &aAmount set successfully!"),

    INVALID_NUMBER("invalid-number", "%prefix% &cInvalid number! (&e%entered%&c)"),

    CANT_ADD_YOURSELF("cant-add-yourself", "%prefix% &cYou can't add yourself to the shop!"),
    CANT_REMOVE_YOURSELF("cant-remove-yourself", "%prefix% &cYou can't remove yourself from the shop!"),
    CANT_CREATE_SHOP_HERE("cant-create-shop-here", "%prefix% &cYou can't create a shop here!"),
    USAGE("usage", "%prefix% &7%usage%"),

    CONFIG_RELOADED("config-reloaded", "%prefix% &aConfig reloaded successfully!"),
    NO_PERMISSION("no-permission", "%prefix% &cYou don't have permission to do that!"),
    NOT_ENOUGH_MONEY("not-enough-money", "%prefix% &cYou don't have enough money! ($%price%)"),
    PLAYER_ONLY("player.only-players", "&cOnly players can use this command!"),
    PLAYER_NOT_FOUND("player.not-found", "%prefix% &cPlayer not found!"),
    PLAYER_ALREADY_ADDED("player.already-added", "%prefix% &cPlayer &f%player-name% &cis already added to the shop!"),
    PLAYER_NOT_ADDED("player-not-added", "%prefix% &cPlayer &f%player-name% &cis not added to the shop!"),
    PLAYER_ADDED("player.added", "%prefix% &aPlayer &f%player-name% &aadded to the shop!"),
    PLAYER_REMOVED("player.removed", "%prefix% &aPlayer &f%player-name% &aremoved from the shop!"),
    PLAYER_NOT_PLAYED_BEFORE("player.not-played-before", "%prefix% &cPlayer &f%player-name% &chas never played before on this server!"),
    SHOP_ALREADY_EXISTS("shop.already-exists", "%prefix% &cShop with name &f%shop-name% &calready exists!"),
    SHOP_BOUGHT("shop.bought", "%prefix% &aYou bought &7x%items-bought% &f%material% &afor &f$%price%&a! &7(&8%items-left%&7)"),
    SHOP_CHEST_ALREADY_SHOP("shop.chest-already-shop", "%prefix% &cThis chest is already a shop!"),
    SHOP_CREATED("shop.created.basic", "%prefix% &aShop &f%shop-name% &acreated!"),
    SHOP_CREATED_PRICE("shop.created.with-price", "%prefix% &aShop &f%shop-name% &acreated for &f$%price%&a!"),
    SHOP_DISABLED_WORLD("shop.disabled-world", "%prefix% &cShops are disabled in this world!"),
    SHOP_INVALID_PAGE("shop.invalid-page", "%prefix% &cInvalid page! &7(&8%page%&7)"),
    SHOP_LIMIT_REACHED("shop.limit-reached", "%prefix% &cYou have reached the shop limit! (&e%shop-limit%&c)"),
    SHOP_PLAYER_LIMIT_REACHED("shop.player-limit-reached", "%prefix% &cYou have reached the player limit! (&e%max-players%&c)"),

    SHOP_LIST_HEAD("shop.list.header", "%prefix% &aYour shops:"),
    SHOP_LIST_SHOPS("shop.list.shops", "&7- &f%shop-name%"),
    SHOP_LIST_SHOPS_HOVER("shop.list.shops-hover", "&6&l%material%&r\n &8- &7price: &6$&e%price%\n &8- &7items left: &e%items-left%\n &8- &7location: &e%location%\n &8- &7created at: &e%created%"),

    SHOP_LIST_CURRENT_PAGE("shop.list.current-page", "&7[&e&l%page%&r&7]"),
    SHOP_LIST_NEXT("shop.list.next", " &7[&eNext&7]"),
    SHOP_LIST_BACK("shop.list.back", "&7[&cBack&7]"),
    SHOP_LIST_PAGE("shop.list.page", "&7[&e%page%&7]"),

    SHOP_BLACKLISTED_NAME("shop.blacklisted-name", "%prefix% &cThis name is blacklisted!"),
    SHOP_NAME_LENGTH("shop.name-length", "%prefix% &cShop name must be between &f%min-length% &cand &f%max-length% &ccharacters long!"),
    SHOP_NO_CHEST("shop.no-chest", "%prefix% &cYou need to look at a chest to create a shop!"),
    SHOP_EMPTY("shop.empty", "%prefix% &cThe shop is empty!"),
    SHOP_INVENTORY_FULL("shop.inventory-full", "%prefix% &cYour inventory is full!"),
    SHOP_EXPLODED("shop.exploded", "%prefix% &cYour %shop-name% shop exploded! &7&o(&f%shop-location%&7)"),
    SHOP_NO_SHOPS("shop.no-shops", "%prefix% &cYou don't have any shops!"),
    SHOP_NOT_FOUND("shop.not-found", "%prefix% &cShop not found!"),
    SHOP_NOT_OWNER("shop.not-owner", "%prefix% &cYou are not the owner of this shop!"),
    SHOP_REMOVED("shop.removed", "%prefix% &cShop &f%shop-name% &cremoved!"),
    SHOP_SETUP_NEEDED("shop.setup-needed", "%prefix% &cThis shop is not set up properly!"),
    SHOP_SOLD("shop.sold", "%prefix% &aYou sold &7x%items-bought% &f%material% &afor &f$%price%&a, to &f%player-name%&a! &7(&8%items-left%&7)"),
    SHOP_RENAMED("shop.renamed", "%prefix% &aShop &f%old-name% &arenamed to &f%new-name%!"),

    ADMIN_SHOP_LIST_HEAD("admin.shop.list.header", "%prefix% &e%player-name%&a's shops:"),
    ADMIN_SHOP_LIST_SHOPS("admin.shop.list.shops", "&7- &f%shop-name%"),
    ADMIN_SHOP_LIST_SHOPS_HOVER("admin.shop.list.shops-hover",
            "&6&l%material%&r\n &8- &7price: &6$&e%price%\n &8- &7items left: &e%items-left%\n &8- &7location: &e%location%\n &8- &7created at: &e%created%"),
    ADMIN_NO_SHOPS("admin.no-shops", "%prefix% &cPlayer &f%player-name% &chas no shops!"),
    ADMIN_CUSTOMER_MODE("admin.customer-mode.message", "%prefix% &aCustomer mode %state%!"),
    ADMIN_CUSTOMER_MODE_OTHER("admin.customer-mode.message-other", "%prefix% &aCustomer mode %state% for &f%player-name%&a!"),
    ADMIN_CUSTOMER_MODE_STATE_DISABLED("admin.customer-mode.disabled", "&cdisabled"),
    ADMIN_CUSTOMER_MODE_STATE_ENABLED("admin.customer-mode.enabled", "&2enabled");

    private String node;
    private String defaultMessage;
    private static MessageConfig config;

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
        return config.getString(node);
    }

    public Component get() {
        return TranslateColor.translate(config.getString(node).replace("%prefix%", config.getString("prefix")));
    }

    public void setDefault() {
        if (config.getString(node) == null) {
            config.set(node, defaultMessage);
        }
        config.saveConfig();
    }

    public static String getPrefix() {
        return config.getString("prefix");
    }

    public static void init(MessageConfig conf) {
        config = conf;
        for (Message message : Message.values()) {
            message.setDefault();
        }
    }
}
