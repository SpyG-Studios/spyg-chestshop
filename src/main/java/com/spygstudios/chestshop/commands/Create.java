package com.spygstudios.chestshop.commands;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.events.ShopCreateEvent;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;
import com.spygstudios.chestshop.shop.ShopUtils;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.milkbowl.vault.economy.Economy;

@Command(name = "spygchestshop create", aliases = { "spcs create", "chestshop create", "scs create" })
public class Create {

    @Execute
    @Permission("spygchestshop.use")
    public void onCreate(@Context Player player, @Arg String name) {
        Block targetBlock = player.getTargetBlock((Set<Material>) null, 4);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            Message.SHOP_NO_CHEST.send(player);
            return;
        }

        if (ShopUtils.isDisabledWorld(player.getWorld().getName())) {
            Message.SHOP_DISABLED_WORLD.send(player);
            return;
        }

        BlockPlaceEvent event = new BlockPlaceEvent(targetBlock, targetBlock.getState(), targetBlock.getRelative(0, -1, 0), new ItemStack(Material.AIR), player, true, EquipmentSlot.HAND);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Message.CANT_CREATE_SHOP_HERE.send(player);
            return;
        }
        Config config = ChestShop.getInstance().getConf();
        double shopPrice = config.getDouble("shops.price");
        Economy economy = ChestShop.getInstance().getEconomy();
        if (shopPrice > 0 && !economy.has(player, config.getDouble("shops.price"))) {
            Message.NOT_ENOUGH_MONEY.send(player, Map.of("%price%", String.valueOf(config.getDouble("shops.price"))));
            return;
        }

        ShopFile file = ShopFile.getShopFile(player);
        if (file == null) {
            file = new ShopFile(ChestShop.getInstance(), player);
        } else if (file.getPlayerShops().contains(name)) {
            Message.SHOP_ALREADY_EXISTS.send(player, Map.of("%shop-name%", name));
            return;
        }

        if (Shop.getShop(targetBlock.getLocation()) != null || (ShopUtils.isDoubleChest(targetBlock) && Shop.getShop(ShopUtils.getAdjacentChest(targetBlock).getLocation()) != null)) {
            Message.SHOP_CHEST_ALREADY_SHOP.send(player);
            return;
        }

        int maxShops = ShopUtils.getMaxShops(player);
        if (maxShops != -1 && file.getPlayerShops().size() >= maxShops) {
            Message.SHOP_LIMIT_REACHED.send(player, Map.of("%shop-limit%", String.valueOf(maxShops)));
            return;
        }

        if (ShopUtils.isBlacklistedName(name)) {
            Message.SHOP_BLACKLISTED_NAME.send(player);
            return;
        }

        int minLength = config.getInt("shops.name.min-length");
        int maxLength = config.getInt("shops.name.max-length");
        if (name.length() < minLength || name.length() > maxLength) {
            Message.SHOP_NAME_LENGTH.send(player, Map.of("%min-length%", minLength + "", "%max-length%", maxLength + ""));
            return;
        }
        Shop shop = new Shop(player, name, targetBlock.getLocation(), file);
        ShopCreateEvent shopCreateEvent = new ShopCreateEvent(shop);
        Bukkit.getPluginManager().callEvent(shopCreateEvent);

        if (shopPrice > 0) {
            economy.withdrawPlayer(player, shopPrice);
            Message.SHOP_CREATED_PRICE.send(player, Map.of("%shop-name%", name, "%price%", String.valueOf(shopPrice)));
            return;
        }
        Message.SHOP_CREATED.send(player, Map.of("%shop-name%", name));
    }
}
