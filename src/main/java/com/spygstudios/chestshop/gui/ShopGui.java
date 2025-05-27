package com.spygstudios.chestshop.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.PageUtil;
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

        if (shop.getAddedPlayers().contains(player.getUniqueId())) {
            Material inventoryMaterial = Material.getMaterial(config.getString("chestshop.inventory.material", "CHEST"));
            ItemStack inventoryItem = ItemUtils.create(inventoryMaterial, config.getString("chestshop.inventory.title"), config.getStringList("chestshop.inventory.lore"));
            PersistentData inventoryData = new PersistentData(plugin, inventoryItem);
            inventoryData.set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
            inventoryData.save();
            inventory.setItem(18, inventoryItem);
        }

        config.getConfigurationSection("shop.amount.items").getKeys(false).forEach(key -> {
            int slot = config.getInt("shop.amount.items." + key + ".slot");
            int amount = config.getInt("shop.amount.items." + key + ".amount");
            String title = config.getString("shop.amount.items." + key + ".title").replace("%amount%", String.valueOf(amount).replace("-", ""));
            List<String> lore = config.getStringList("shop.amount.items." + key + ".lore");
            Material material = Material.getMaterial(config.getString("shop.amount.items." + key + ".material", "GRAY_STAINED_GLASS_PANE"));
            addItemToInventory(plugin, inventory, slot, material, title, lore, amount);
        });

        PageUtil.setFillItems(inventory, "shop");
        player.openInventory(inventory);

    }

    private void addItemToInventory(ChestShop plugin, Inventory inventory, int slot, Material material, String title, List<String> lore, int amount) {
        ItemStack item = ItemUtils.create(material, title, lore, Math.abs(amount));
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
