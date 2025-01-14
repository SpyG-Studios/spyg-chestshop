package com.spygstudios.chestshop.gui;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import com.spygstudios.spyglib.item.PlayerHeads;
import com.spygstudios.spyglib.persistentdata.PersistentData;
import com.spygstudios.spyglib.placeholder.ParseListPlaceholder;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
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
        ItemStack shopMaterial;
        if (shop.getMaterial() != null) {
            shopMaterial = new ItemStack(shop.getMaterial());
        } else {
            shopMaterial = ItemUtils.create(Material.BARRIER, config.getString("shop.material.title"), config.getStringList("shop.material.lore"));
        }
        inventory.setItem(13, shopMaterial);
        PersistentData materialData = new PersistentData(plugin, inventory.getItem(13));
        materialData.set("action", GuiAction.SET_MATERIAL.name());
        materialData.save();

        // info item
        ItemStack infoItem = ItemUtils.create(Material.WRITABLE_BOOK, config.getString("shop.info.title"),
                ParseListPlaceholder.parse(config.getStringList("shop.info.lore"),
                        Map.of("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName(), "%material%", shop.getMaterial() == null ? "AIR" : shop.getMaterial().name(), "%price%",
                                String.valueOf(shop.getPrice()), "%created%", shop.getCreatedAt(), "%location%", shop.getChestLocationString(), "%sold-items%", String.valueOf(shop.getSoldItems()),
                                "%money-earnd%", String.valueOf(shop.getMoneyEarned()))));
        inventory.setItem(8, infoItem);

        // notify item
        ItemStack notifyItem = ItemUtils.create(Material.BELL, config.getString("shop.notify.title"),
                Arrays.asList(shop.isNotify() ? config.getString("shop.notify.on") : config.getString("shop.notify.off")));
        PersistentData notifyData = new PersistentData(plugin, notifyItem);
        notifyData.set("action", GuiAction.TOGGLE_NOTIFY.name());
        notifyData.set("shop", shop.getName());
        notifyData.save();
        inventory.setItem(0, notifyItem);

        // money item
        ItemStack moneyItem = ItemUtils.create(Material.GOLD_INGOT, config.getString("shop.money.title"), config.getStringList("shop.money.lore"));
        PersistentData moneyData = new PersistentData(plugin, moneyItem);
        moneyData.set("action", GuiAction.SET_ITEM_PRICE.name());
        moneyData.set("shop", shop.getName());
        moneyData.save();
        inventory.setItem(11, moneyItem);
      
        // inventory item
        ItemStack inventoryItem = ItemUtils.create(Material.CHEST, config.getString("shop.inventory.title"), config.getStringList("shop.inventory.lore"));
        PersistentData inventoryData = new PersistentData(plugin, inventoryItem);
        inventoryData.set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
        inventoryData.set("shop", shop.getName());
        inventoryData.save();
        inventory.setItem(18, inventoryItem);

        // player item
        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
        ItemStack playrItem = owner.isOnline() ? PlayerHeads.getOnlinePlayerHead(owner.getUniqueId()) : PlayerHeads.getOfflinePlayerHead(owner.getUniqueId());
        ItemMeta playrMeta = playrItem.getItemMeta();
        playrMeta.displayName(TranslateColor.translate(config.getString("shop.player.title").replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));
        playrItem.setItemMeta(playrMeta);
        PersistentData playerData = new PersistentData(plugin, playrItem);
        playerData.set("action", GuiAction.OPEN_PLAYERS.name());
        playerData.set("shop", shop.getName());
        playerData.save();
        inventory.setItem(26, playrItem);
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
