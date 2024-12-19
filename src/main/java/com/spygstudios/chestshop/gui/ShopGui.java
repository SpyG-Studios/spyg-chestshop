package com.spygstudios.chestshop.gui;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.persistentdata.PersistentData;
import com.spygstudios.spyglib.placeholder.ParseListPlaceholder;

import lombok.Getter;

public class ShopGui {
    private static GuiConfig config;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new ShopHolder(player, shop), 27,
                TranslateColor.translate(config.getString("shop.title").replace("%shop-name%", shop.getName()).replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));
        setShopItems(plugin, shop, inventory);
        player.openInventory(inventory);
    }

    private static void setShopItems(ChestShop plugin, Shop shop, Inventory inventory) {
        if (shop.getMaterial() != null) {
            inventory.setItem(13, new ItemStack(shop.getMaterial()));
            PersistentData materialData = new PersistentData(plugin, inventory.getItem(13));
            materialData.set("action", "change-material");
            materialData.save();
        }

        // info item
        ItemStack infoItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(TranslateColor.translate(config.getString("shop.info.title")));
        infoMeta.lore(TranslateColor.translate(ParseListPlaceholder.parse(config.getStringList("shop.info.lore"),
                Map.of("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName(), "%material%", shop.getMaterial() == null ? "AIR" : shop.getMaterial().name(), "%price%",
                        String.valueOf(shop.getPrice()), "%created%", shop.getCreatedAt(), "%location%", shop.getChestLocationString(), "%amount%", String.valueOf(shop.getAmount()), "%sold-items%",
                        String.valueOf(shop.getSoldItems()), "%money-earnd%", String.valueOf(shop.getMoneyEarned())))));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(8, infoItem);

        // notify item
        ItemStack notifyItem = new ItemStack(Material.BELL);
        ItemMeta notifyMeta = notifyItem.getItemMeta();
        notifyMeta.displayName(TranslateColor.translate(config.getString("shop.notify.title")));
        notifyMeta.lore(Arrays.asList(TranslateColor.translate(shop.isNotify() ? config.getString("shop.notify.on") : config.getString("shop.notify.off"))));
        notifyItem.setItemMeta(notifyMeta);
        PersistentData notifyData = new PersistentData(plugin, notifyItem);
        notifyData.set("action", "toggle-notify");
        notifyData.set("shop", shop.getName());
        notifyData.save();
        inventory.setItem(0, notifyItem);
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
