package com.spygstudios.chestshop.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.item.ItemUtils;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class ShopGui {

    public static void open(ChestShop plugin, Player player, Shop shop) {
        GuiConfig config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new ShopGuiHolder(player, shop), 27, TranslateColor.translate(config.getString("shop.title").replace("%shop-name%", shop.getName())));

        // Set shop material
        ItemStack shopItem = new ItemStack(shop.getMaterial());
        ItemMeta shopMeta = shopItem.getItemMeta();
        shopMeta.displayName(TranslateColor.translate(config.getString("shop.item-to-buy.title").replace("%material%", shop.getMaterial().name())));
        List<Component> translatedLore = plugin.getGuiConfig().getStringList("shop.item-to-buy.lore").stream()
                .map(line -> TranslateColor.translate(line.replace("%price%", String.valueOf(shop.getPrice())))).toList();
        shopMeta.lore(translatedLore);
        shopItem.setItemMeta(shopMeta);
        PersistentData data = new PersistentData(plugin, shopItem);
        data.set("action", GuiAction.BUY.name());
        data.save();
        inventory.setItem(13, shopItem);

        addItemToInventory(plugin, inventory, 9, Material.RED_STAINED_GLASS_PANE, config.getString("shop.minus.title").replace("%amount%", config.getString("shop.amount.items.1")),
                config.getStringList("shop.minus.lore"), config.getInt("shop.amount.items.1"));
        addItemToInventory(plugin, inventory, 11, Material.RED_STAINED_GLASS_PANE, config.getString("shop.minus.title").replace("%amount%", config.getString("shop.amount.items.2")),
                config.getStringList("shop.minus.lore"), config.getInt("shop.amount.items.2"));
        addItemToInventory(plugin, inventory, 15, Material.GREEN_STAINED_GLASS_PANE, config.getString("shop.plus.title").replace("%amount%", config.getString("shop.amount.items.3")),
                config.getStringList("shop.plus.lore"), config.getInt("shop.amount.items.3"));
        addItemToInventory(plugin, inventory, 17, Material.GREEN_STAINED_GLASS_PANE, config.getString("shop.plus.title").replace("%amount%", config.getString("shop.amount.items.4")),
                config.getStringList("shop.plus.lore"), config.getInt("shop.amount.items.4"));

        player.openInventory(inventory);
    }

    private void addItemToInventory(ChestShop plugin, Inventory inventory, int slot, Material material, String title, List<String> lore, int amount) {
        ItemStack item = ItemUtils.create(material, title, lore);
        PersistentData data = new PersistentData(plugin, item);
        data.set("action", GuiAction.SET_ITEM_AMOUNT.name());
        data.set("amount", amount);
        data.save();
        inventory.setItem(slot, item);
    }

    public static class ShopGuiHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final Shop shop;

        public ShopGuiHolder(Player player, Shop shop) {
            this.player = player;
            this.shop = shop;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
