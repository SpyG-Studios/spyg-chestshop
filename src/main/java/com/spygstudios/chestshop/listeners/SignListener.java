package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopFile;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.components.ComponentUtils;

public class SignListener implements Listener {

    private final Config config;

    public SignListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = plugin.getConf();
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getSide().equals(Side.BACK) && !isSignInFrontOfChest(event.getBlock())) {
            return;
        }
        if (config.getStringList("shop.triggers").stream().noneMatch(trigger -> trigger.equalsIgnoreCase(ComponentUtils.fromComponent(event.line(0))))) {
            return;
        }

        Player player = event.getPlayer();
        Block targetBlock = getAttachedChest(event.getBlock());
        if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
            Message.SHOP_NO_CHEST.send(player);
            return;
        }

        if (Shop.isDisabledWorld(player.getWorld())) {
            Message.SHOP_DISABLED_WORLD.send(player);
            return;
        }

        String name = ComponentUtils.fromComponent(event.line(1)).trim();
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

        if (config.getInt("shops.max-shops") != 0 && file.getPlayerShops().size() >= config.getInt("shops.max-shops")) {
            Message.SHOP_LIMIT_REACHED.send(player, Map.of("%shop-limit%", String.valueOf(config.getInt("shops.max-shops"))));
            return;
        }

        file.addShop(player, name, targetBlock.getLocation());
        for (int i = 0; i < 4; i++) {
            event.line(i, TranslateColor
                    .translate(config.getString("shop.sign.line." + (i + 1)).replace("%owner%", player.getName()).replace("%amount%", "0").replace("%price%", "0").replace("%material%", "-")));
        }
        Message.SHOP_CREATED.send(player, Map.of("%shop-name%", name));
    }

    private boolean isSignInFrontOfChest(Block signBlock) {
        if (!(signBlock.getBlockData() instanceof WallSign)) {
            return false;
        }

        WallSign wallSign = (WallSign) signBlock.getBlockData();
        Block attachedBlock = getAttachedChest(signBlock);

        // Ellenőrizzük, hogy az adott blokk egy láda-e
        if (attachedBlock.getType() == Material.CHEST) {
            // Ellenőrizzük, hogy a tábla valóban a láda elején van-e
            Chest chest = (Chest) attachedBlock.getBlockData();
            return chest.getFacing() == wallSign.getFacing();
        }

        return false;
    }

    private Block getAttachedChest(Block signBlock) {
        if (!(signBlock.getBlockData() instanceof WallSign)) {
            return null;
        }

        WallSign wallSign = (WallSign) signBlock.getBlockData();
        return signBlock.getRelative(wallSign.getFacing().getOppositeFace());
    }

}
