package com.spygstudios.chestshop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import lombok.Getter;

public class ShopGui {
    public static void open(ChestShop plugin, Player player, Shop shop) {
        GuiConfig config = plugin.getGuisConfig();
        Inventory inventory = player.getServer().createInventory(new ShopHolder(player, shop), 27,
                TranslateColor.translate(config.getString("shop.title").replace("%shop-name%", shop.getName()).replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwner()).getName())));
        if (shop.getMaterial() != null) {
            inventory.setItem(13, new ItemStack(shop.getMaterial()));
            PersistentData data = new PersistentData(plugin, inventory.getItem(13));
            data.set("action", "change-material");
            data.save();
        }
        player.openInventory(inventory);
    }

    public static class ShopHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final Material material;

        @Getter
        private final Shop shop;

        public ShopHolder(Player player, Shop shop) {
            this.player = player;
            this.material = shop.getMaterial() == null ? Material.AIR : shop.getMaterial();
            this.shop = shop;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
