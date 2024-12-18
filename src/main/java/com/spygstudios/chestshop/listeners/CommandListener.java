package com.spygstudios.chestshop.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
import com.spygstudios.spyglib.components.ComponentUtils;
import com.spygstudios.spyglib.inventory.InventoryUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;

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
                String pageString = args.length == 1 ? "1" : args[1];

                if (!isPositiveInteger(pageString)) {
                    ComponentUtils.replaceComponent(Message.SHOP_INVALID_PAGE.get(), Map.of("%page%", pageString));
                    return true;
                }

                int page = Integer.parseInt(pageString);
                ShopFile file = ShopFile.getShopFile(player);
                if (file == null || file.getPlayerShops().isEmpty()) {
                    Message.SHOP_NO_SHOPS.sendMessage(player);
                    return true;
                }
                Message.SHOP_LIST_HEAD.sendMessage(player);
                List<Shop> shops = Shop.getShops(player).stream().sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).skip((page - 1) * 10).limit(10).collect(Collectors.toList());

                for (Shop shop : shops) {
                    Chest chest = (Chest) shop.getChestLocation().getBlock().getState();
                    String itemsLeft = String.valueOf(InventoryUtils.countItems(chest.getInventory(), shop.getMaterial()));
                    Message.SHOP_LIST_SHOPS.sendMessage(player, Map.of("%shop-name%", shop.getName(), "%material%", shop.getMaterialString(), "%price%", shop.getPrice() + "", "%amount%",
                            shop.getAmount() + "", "%items-left%", itemsLeft));
                }

                int pages = (int) Math.ceil((double) file.getPlayerShops().size() / 10);
                Component message;
                Component back = Message.SHOP_LIST_BACK.get().clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/%command% list ".replace("%command%", label) + (page - 1)));
                Component next = Message.SHOP_LIST_NEXT.get().clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/%command% list ".replace("%command%", label) + (page + 1)));
                Builder pagesComponent = Component.text();

                for (int i = 0; i < pages; i++) {
                    if (i + 1 == page) {
                        pagesComponent.append(ComponentUtils.replaceComponent(Message.SHOP_LIST_CURRENT_PAGE.get(), "%page%", String.valueOf(i + 1)));
                        continue;
                    }
                    pagesComponent.append(ComponentUtils.replaceComponent(Message.SHOP_LIST_PAGE.get(), "%page%", String.valueOf(i + 1)))
                            .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/%command% list ".replace("%command%", label) + (i + 1)));
                }

                if (pages == 1) {
                    message = pagesComponent.build(); // Csak egy oldal van
                } else if (page < pages && page == 1) {
                    message = Component.text().append(pagesComponent.build()).append(next).build(); // Van még oldal, de az elsőn vagyunk
                } else if (page < pages) {
                    message = Component.text().append(back).append(pagesComponent.build()).append(next).build(); // Van még oldal, de nem az elsőn vagyunk
                } else {
                    message = Component.text().append(back).append(pagesComponent.build()).build(); // Az utolsó oldalon vagyunk
                }

                player.sendMessage(message);
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

    public static boolean isPositiveInteger(String str) {
        return str != null && str.matches("[1-9][0-9]*");
    }

}
