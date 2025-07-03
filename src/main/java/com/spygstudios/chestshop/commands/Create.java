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
import com.spygstudios.chestshop.events.ShopCreatedEvent;
import com.spygstudios.chestshop.interfaces.DataManager;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.chestshop.shop.yaml.YamlShopFile;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop create", aliases = { "spcs create", "chestshop create", "scs create" })
public class Create {

    private final ChestShop plugin;

    public Create(ChestShop plugin) {
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.use")
    @Permission("spygchestshop.*")
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

        YamlShopFile file = YamlShopFile.getShopFile(player);
        if (file == null) {
            file = new YamlShopFile(ChestShop.getInstance(), player);
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
        Config config = ChestShop.getInstance().getConf();

        int minLength = config.getInt("shops.name.min-length");
        int maxLength = config.getInt("shops.name.max-length");
        if (name.length() < minLength || name.length() > maxLength) {
            Message.SHOP_NAME_LENGTH.send(player, Map.of("%min-length%", minLength + "", "%max-length%", maxLength + ""));
            return;
        }

        DataManager shopData = plugin.getDataManager();
        shopData.createShop(player.getUniqueId(), name, targetBlock.getLocation(), shopData.getDateString(), shop -> {
            if (shop == null) {
                plugin.getLogger().warning("Failed to create shop for " + player.getName() + " at " + targetBlock.getLocation());
                return;
            }

            ShopCreatedEvent shopCreateEvent = new ShopCreatedEvent(shop);
            Bukkit.getPluginManager().callEvent(shopCreateEvent);
            Message.SHOP_CREATED.send(player, Map.of("%shop-name%", name));
        });

    }
}
