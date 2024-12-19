package com.spygstudios.chestshop.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuisConfig;
import com.spygstudios.spyglib.color.TranslateColor;

import lombok.Getter;

public class MainGui {
    public static void open(ChestShop plugin, Player player, String material, int amount, double price) {
        GuisConfig config = plugin.getGuisConfig();
        Inventory inventory = player.getServer().createInventory(new ShopHolder(player, material, amount, price), 27,
                TranslateColor.translate(config.getString("item_adding.title").replace("%material%", material).replace("%amount%", String.valueOf(amount))));

        player.openInventory(inventory);
    }

    public static class ShopHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final String material;

        @Getter
        private final int amount;

        @Getter
        private final double price;

        public ShopHolder(Player player, String material, int amount, double price) {
            this.player = player;
            this.material = material;
            this.amount = amount;
            this.price = price;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
