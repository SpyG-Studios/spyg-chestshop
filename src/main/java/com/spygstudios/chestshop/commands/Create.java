package com.spygstudios.chestshop.commands;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;

@Command(name = "spygchestshop create", aliases = { "spcs create", "chestshop create", "scs create" })
public class Create {

    @Execute
    public void onCreate(@Context Player player, @Arg String name) {
        Block targetBlock = player.getTargetBlock((Set<Material>) null, 4);
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            player.sendMessage("You must be looking at a chest to create a shop.");
            return;
        }

        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            Message.SHOP_NO_CHEST.send(player);
            return;
        }

        if (Shop.isDisabledWorld(player.getWorld())) {
            Message.SHOP_DISABLED_WORLD.send(player);
            return;
        }

        BlockBreakEvent event = new BlockBreakEvent(targetBlock, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Message.CANT_CREATE_SHOP_HERE.send(player);
            return;
        }

        if (name.length() < 3) {
            Message.SHOP_NAME_TOO_SHORT.send(player);
            return;
        }
        ShopFile file = ShopFile.getShopFile(player);
        if (file == null) {
            file = new ShopFile(ChestShop.getInstance(), player);
        } else if (file.getPlayerShops().contains(name)) {
            Message.SHOP_ALREADY_EXISTS.send(player, Map.of("%shop-name%", name));
            return;
        }

        if (Shop.getShop(targetBlock.getLocation()) != null || (Shop.isDoubleChest(targetBlock) && Shop.getShop(Shop.getAdjacentChest(targetBlock).getLocation()) != null)) {
            Message.SHOP_CHEST_ALREADY_SHOP.send(player);
            return;
        }

        Config config = ChestShop.getInstance().getConf();

        if (config.getInt("shops.max-shops") != 0 && file.getPlayerShops().size() >= config.getInt("shops.max-shops")) {
            Message.SHOP_LIMIT_REACHED.send(player, Map.of("%shop-limit%", String.valueOf(config.getInt("shops.max-shops"))));
            return;
        }

        file.addShop(player, name, targetBlock.getLocation());
        Message.SHOP_CREATED.send(player, Map.of("%shop-name%", name));
    }
}
