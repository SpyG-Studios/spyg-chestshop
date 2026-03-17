package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class Config extends YamlManager {

    public Config(ChestShop plugin) {
        super("config.yml", plugin);
        setOrDefault("check-for-updates", true, Arrays.asList("Check for updates on join (spygchestshop.admin.updates)."));
        setOrDefault("locale", "en_US", Arrays.asList("The locale of the plugin. (file name)"));
        setOrDefault("cancel", "cancel", Arrays.asList("The text to cancel the amount action."));
        // set("storage-type", "yaml", Arrays.asList("The storage type of the plugin.
        // (yaml, sqlite, mysql)"));
        setOrDefault("mysql.host", "localhost", Arrays.asList("The host of the MySQL database."));
        setOrDefault("mysql.port", 3306, Arrays.asList("The port of the MySQL database."));
        setOrDefault("mysql.database", "chestshop", Arrays.asList("The database name of the MySQL database."));
        setOrDefault("mysql.username", "root", Arrays.asList("The username of the MySQL database."));
        setOrDefault("mysql.password", "password", Arrays.asList("The password of the MySQL database."));
        setOrDefault("colors.command.required-arg", " &c");
        setOrDefault("colors.command.optional-arg", " &7");
        setOrDefault("colors.command.list", "&8- ");
        setOrDefault("colors.command.label", "&7");
        setOrDefault("colors.command.args", "&f");

        setOrDefault("shops.price", 0.0, Arrays.asList("Shop creation price. Set to 0 for free shops."));
        setOrDefault("shops.minimum-durability", 100, Arrays.asList("The minimum durability of the item to be used in a shop. (in percentage)"));
        setOrDefault("shops.price-format.sell", "&4$&c%price% &7x %quantity%", Arrays.asList("The format to use for the sell price in the shop hologram. (%sell-price%, %quantity%)"));
        setOrDefault("shops.price-format.buy", "&2$&a%price% &7x %quantity%", Arrays.asList("The format to use for the buy price in the shop hologram. (%buy-price%, %quantity%)"));
        setOrDefault("shops.price-format.combined", "%buy-price% &f- %sell-price%", Arrays.asList("The format to use for the combined price in the shop hologram. (%price%)"));
        setOrDefault("shops.decimals.enabled", false, Arrays.asList("Enable cents in the shops. This will allow prices to have decimal values (e.g. 1.99)."));
        setOrDefault("shops.decimals.max", 2, Arrays.asList("The maximum amount of decimal places allowed in the price."));
        setOrDefault("shops.barrier-when-empty", false, Arrays.asList("Displays a barrier instead of the shop's item above the shop when it is out of stock."));
        setOrDefault("shops.name.max-length", 16);
        setOrDefault("shops.name.min-length", 3);
        setOrDefault("shops.blacklisted-names", Arrays.asList("stupid"));
        setOrDefault("shops.disabled-worlds", Arrays.asList("disabled_world"));
        setOrDefault("shops.only-in-regions", false, Arrays.asList("Players can only create shops in specified WorldGuard regions."));
        setOrDefault("shops.unknown.item", "-", Arrays.asList("The text to use when the item is unknown."));
        setOrDefault("shops.unknown.mode", "&cOffline", Arrays.asList("The text to use when the shop mode is not set."));
        setOrDefault("shops.unknown.owner", "Unknown Owner", Arrays.asList("The text to use when the shop owner is unknown."));
        setOrDefault("shops.max-shops.default", -1, Arrays.asList(
                "The maximum amount of shops a player can have (spygchestshop.max.<shop_group_name>). Set to -1 for unlimited. If a player has more than one permission, the highest amount will be used."));
        setOrDefault("shops.max-players", 18, Arrays.asList("The maximum amount of players that can be added to a shop. Set to 0 for unlimited."));
        setOrDefault("shops.save-interval", 60, Arrays.asList("The interval in seconds to save the shops data."));
        setOrDefault("shops.holograms.range", 16, Arrays.asList("The view range of the holograms. Restart required to apply changes."));
        setOrDefault("shops.holograms.see-through-walls", false, Arrays.asList("If the holograms should be visible through walls."));

        setOrDefault("shops.anti-explosion", true, Arrays.asList("Prevent shops from being destroyed by explosions."));
        setOrDefault("shops.hopper-protection", true,
                Arrays.asList("Prevent hoppers from taking items from shops. !!!YOU MUST TURN OFF \"hopper.disable-move-event\" IN PAPER CONFIG IN ORDER TO PROTECT THE HOPPERS!!!"));
        setOrDefault("shops.lines", Arrays.asList("%shop-name%", "&#dddddd%owner%", "&a%price%", "&7[%items-left%]"), Arrays.asList("The lines of the shop."));
        saveConfig();
    }

}
