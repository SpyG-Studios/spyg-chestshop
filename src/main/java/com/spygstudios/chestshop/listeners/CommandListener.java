package com.spygstudios.chestshop.listeners;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.components.ComponentUtils;

public class CommandListener implements CommandExecutor, Listener {

    private Config config;

    public CommandListener(ChestShop plugin, String command) {
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(new TabListener());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = plugin.getConf();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.getMessage("player-only"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(TranslateColor.translate("&cUsage: /chestshop <create|remove|list>"));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("create")) {
                player.sendMessage(TranslateColor.translate("&cUsage: /chestshop create <name>"));
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                config.reloadConfig();
                player.sendMessage(config.getMessage("config-reloaded"));
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                Block targetBlock = player.getTargetBlock((Set<Material>) null, 4);
                if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
                    player.sendMessage(config.getMessage("no-chest"));
                    return true;
                }

                if (Shop.isDisabledWorld(player.getWorld())) {
                    player.sendMessage(config.getMessage("disabled-world"));
                    return true;
                }

                String name = args[1].trim();
                ShopFile file = ShopFile.getShopFile(player);
                if (file == null) {
                    file = new ShopFile(ChestShop.getInstance(), player);
                } else if (file.getPlayerShops().contains(name)) {
                    player.sendMessage(ComponentUtils.replaceComponent(config.getMessage("shop.already-exists"), "%shop-name%", name));
                    return true;
                }

                if (Shop.getShop(targetBlock.getLocation()) != null || (Shop.isDoubleChest(targetBlock) && Shop.getShop(Shop.getAdjacentChest(targetBlock).getLocation()) != null)) {
                    player.sendMessage(config.getMessage("shop.chest-already-shop"));
                    return true;
                }

                if (!Shop.isChestFaceFree(targetBlock)) {
                    player.sendMessage(config.getMessage("chest-face-not-free"));
                    return true;
                }

                if (config.getInt("shops.max-shops") != 0 && file.getPlayerShops().size() >= config.getInt("shops.max-shops")) {
                    player.sendMessage(ComponentUtils.replaceComponent(config.getMessage("shop.limit-reached"), "%shop-limit%", String.valueOf(config.getInt("shops.max-shops"))));
                    return true;
                }

                file.addShop(new Shop(player, name, targetBlock.getLocation(), null, 0, 0));
                player.sendMessage(ComponentUtils.replaceComponent(config.getMessage("shop.created"), "%shop-name%", name));
                return true;
            }
        }
        return true;
    }

}
