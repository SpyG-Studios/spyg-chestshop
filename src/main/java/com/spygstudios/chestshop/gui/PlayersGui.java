package com.spygstudios.chestshop.gui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;

import lombok.Getter;

public class PlayersGui {
    private static GuiConfig config;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new PlayersHolder(player, shop), 27,
                TranslateColor.translate(config.getString("players.title").replace("%shop-name%", shop.getName())));
        shop.getAddedPlayers().forEach(uuid -> inventory.addItem(getPlayerHead(uuid)));
        player.openInventory(inventory);
    }

    private static ItemStack getPlayerHead(UUID uuid) {
        ItemStack playerHead = new ItemStack(org.bukkit.Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            playerHead.setItemMeta(skullMeta);
        }

        return playerHead;
    }

    public static class PlayersHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final Shop shop;

        public PlayersHolder(Player player, Shop shop) {
            this.player = player;
            this.shop = shop;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
