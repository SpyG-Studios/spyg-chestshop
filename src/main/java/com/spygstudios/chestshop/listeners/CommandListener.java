package com.spygstudios.chestshop.listeners;

import java.util.List;
import java.util.Map;
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
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;
import com.spygstudios.spyglib.color.TranslateColor;

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
            Message.PLAYER_ONLY.sendMessage(sender);
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
                Message.CONFIG_RELOADED.sendMessage(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                ShopFile file = ShopFile.getShopFile(player);
                if (file == null || file.getPlayerShops().isEmpty()) {
                    Message.SHOP_NO_SHOPS.sendMessage(player);
                    return true;
                }
                List<Shop> shops = Shop.getShops(player);
                Message.SHOP_LIST_HEAD.sendMessage(player);
                for (Shop shop : shops) {
                    Message.SHOP_LIST_SHOPS.sendMessage(player,
                            Map.of("%shop-name%", shop.getName(), "%material%", shop.getMaterialString(), "%price%", shop.getPrice() + "", "%amount%", shop.getAmount() + ""));
                }
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                Block targetBlock = player.getTargetBlock((Set<Material>) null, 4);
                if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
                    Message.SHOP_NO_CHEST.sendMessage(player);
                    return true;
                }

                if (Shop.isDisabledWorld(player.getWorld())) {
                    Message.SHOP_DISABLED_WORLD.sendMessage(player);
                    return true;
                }

                String name = args[1].trim();
                ShopFile file = ShopFile.getShopFile(player);
                if (file == null) {
                    file = new ShopFile(ChestShop.getInstance(), player);
                } else if (file.getPlayerShops().contains(name)) {
                    Message.SHOP_ALREADY_EXISTS.sendMessage(player, Map.of("%shop-name%", name));
                    return true;
                }

                if (Shop.getShop(targetBlock.getLocation()) != null || (Shop.isDoubleChest(targetBlock) && Shop.getShop(Shop.getAdjacentChest(targetBlock).getLocation()) != null)) {
                    Message.SHOP_CHEST_ALREADY_SHOP.sendMessage(player);
                    return true;
                }

                if (!Shop.isChestFaceFree(targetBlock)) {
                    Message.SHOP_CHEST_FACE_NOT_FREE.sendMessage(player);
                    return true;
                }

                if (config.getInt("shops.max-shops") != 0 && file.getPlayerShops().size() >= config.getInt("shops.max-shops")) {
                    Message.SHOP_LIMIT_REACHED.sendMessage(player, Map.of("%shop-limit%", String.valueOf(config.getInt("shops.max-shops"))));
                    return true;
                }

                file.addShop(new Shop(player, name, targetBlock.getLocation(), null, 0, 0, false));
                Message.SHOP_CREATED.sendMessage(player, Map.of("%shop-name%", name));
                return true;
            }
        }
        return true;
    }

}
