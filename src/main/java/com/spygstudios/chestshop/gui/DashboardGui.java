package com.spygstudios.chestshop.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.spygstudios.chestshop.PageUtil;
import com.spygstudios.chestshop.config.Config;
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
public class DashboardGui {
    private static GuiConfig guiConfig;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        guiConfig = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new DashboardHolder(player, shop), 27,
                TranslateColor.translate(guiConfig.getString("chestshop.title")
                        .replace("%shop-name%", shop.getName())
                        .replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));
        setShopItems(plugin, shop, inventory);
        PageUtil.setFillItems(inventory, "chestshop");
        player.openInventory(inventory);
    }

    private static void setShopItems(ChestShop plugin, Shop shop, Inventory inventory) {
        Config config = plugin.getConf();
        ItemStack shopMaterial = shop.getMaterial() != null
                ? new ItemStack(shop.getMaterial())
                : ItemUtils.create(Material.BARRIER, guiConfig.getString("chestshop.material.title"), guiConfig.getStringList("chestshop.material.lore"));
                        Material.getMaterial(guiMaterialSection.getString("not-set.material", "BARRIER")),
        PersistentData materialData = new PersistentData(plugin, inventory.getItem(13));
        materialData.set("action", GuiAction.SET_MATERIAL.name());

        // info item
        String buyPrice = config.getString("shops.price-format.buy")
                .replace("%price%", String.valueOf(shop.getCustomerPurchasePrice()));
        String sellPrice = config.getString("shops.price-format.sell")
                .replace("%price%", String.valueOf(shop.getCustomerSalePrice()));
        Material infoMaterial = Material.getMaterial(config.getString("chestshop.info.material", "WRITABLE_BOOK"));
        String priceDisplay = "";
        if (shop.acceptsCustomerPurchases() && shop.acceptsCustomerSales()) {
            priceDisplay = config.getString("shops.price-format.combined")
                    .replace("%sell-price%", sellPrice)
                    .replace("%buy-price%", buyPrice);
        } else if (shop.acceptsCustomerPurchases()) {
            priceDisplay = buyPrice;
        } else if (shop.acceptsCustomerSales()) {
            priceDisplay = sellPrice;
        } else {
            priceDisplay = config.getString("shops.unknown.mode");
        }
        ItemStack infoItem = ItemUtils.create(infoMaterial, guiConfig.getString("chestshop.info.title"),
                ParseListPlaceholder.parse(guiConfig.getStringList("chestshop.info.lore"), Map.of(
                        "%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName(),
                        "%material%", shop.getMaterial() == null ? "AIR" : shop.getMaterial().name(),
                        "%price%", priceDisplay,
                        "%created%", shop.getCreatedAt(),
                        "%location%", shop.getChestLocationString(),
                        "%sold-items%", String.valueOf(shop.getSoldItems()),
                        "%money-earned%", String.valueOf(shop.getMoneyEarned()))));
                infoSection.getFloatList("model-data.floats"),

        // notify item
        Material notifyMaterial = Material.getMaterial(guiConfig.getString("chestshop.notify.material", "BELL"));
        ItemStack notifyItem = ItemUtils.create(notifyMaterial, guiConfig.getString("chestshop.notify.title"),
                Arrays.asList(shop.isNotify()
                        ? guiConfig.getString("chestshop.notify.on")
                        : guiConfig.getString("chestshop.notify.off")));
        PersistentData notifyData = new PersistentData(plugin, notifyItem);
        notifyData.set("action", GuiAction.TOGGLE_NOTIFY.name());
        inventory.setItem(notifySection.getInt("slot"), notifyItem);

        // money item
        Material moneyMaterial = Material.getMaterial(guiConfig.getString("chestshop.money.material", "GOLD_INGOT"));
        List<String> moneyLore = new ArrayList<>();
        for (String string : guiConfig.getStringList("chestshop.money.lore")) {
            moneyLore.add(string
                    .replace("%sell-price%", String.valueOf(shop.getCustomerPurchasePrice()))
                    .replace("%buy-price%", String.valueOf(shop.getCustomerSalePrice())));
        }

        ItemStack moneyItem = ItemUtils.create(moneyMaterial, guiConfig.getString("chestshop.money.title"), moneyLore);
        PersistentData moneyData = new PersistentData(plugin, moneyItem);
        moneyData.set("action", GuiAction.SET_SHOP_BUY_PRICE.name());
        inventory.setItem(moneySection.getInt("slot"), moneyItem);

        // inventory item
        Material inventoryMaterial = Material.getMaterial(guiConfig.getString("chestshop.inventory.material", "CHEST"));
        ItemStack inventoryItem = ItemUtils.create(inventoryMaterial, guiConfig.getString("chestshop.inventory.title"), guiConfig.getStringList("chestshop.inventory.lore"));
        PersistentData inventoryData = new PersistentData(plugin, inventoryItem);
        inventoryData.set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
        inventory.setItem(inventorySection.getInt("slot"), inventoryItem);

        // buy/sell toggle item
        Material buySellMaterial = Material.getMaterial(guiConfig.getString("chestshop.buysell.material", "COMPARATOR"));
        List<String> buySellLore = new ArrayList<>();
        String sellStatus = shop.acceptsCustomerPurchases()
                ? guiConfig.getString("chestshop.buysell.sell.enabled", "&aEnabled")
                : guiConfig.getString("chestshop.buysell.sell.disabled", "&cDisabled");
        String buyStatus = shop.acceptsCustomerSales()
                ? guiConfig.getString("chestshop.buysell.buy.enabled", "&aEnabled")
                : guiConfig.getString("chestshop.buysell.buy.disabled", "&cDisabled");
        buySellLore.add(guiConfig.getString("chestshop.buysell.sell.line", "&7Selling: %status%").replace("%status%", sellStatus));
        buySellLore.add(guiConfig.getString("chestshop.buysell.buy.line", "&7Buying: %status%").replace("%status%", buyStatus));
        buySellLore.addAll(guiConfig.getStringList("chestshop.buysell.lore"));
        ItemStack buySellItem = ItemUtils.create(buySellMaterial, guiConfig.getString("chestshop.buysell.title"), buySellLore);
        PersistentData buySellData = new PersistentData(plugin, buySellItem);
        buySellData.set("action", GuiAction.TOGGLE_SELLING.name());
        inventory.setItem(buySellSection.getInt("slot"), buySellItem);

        // player item
        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
        ItemStack playrItem = owner.isOnline() ? PlayerHeads.getOnlinePlayerHead(owner.getUniqueId()) : PlayerHeads.getOfflinePlayerHead(owner.getUniqueId());
        ItemMeta playrMeta = playrItem.getItemMeta();
        playrMeta.displayName(TranslateColor.translate(guiConfig.getString("chestshop.player.title").replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));
        playrMeta.lore(TranslateColor.translate(guiConfig.getStringList("chestshop.player.lore")));
        playrItem.setItemMeta(playrMeta);
        PersistentData playerData = new PersistentData(plugin, playrItem);
        playerData.set("action", GuiAction.OPEN_PLAYERS.name());
        inventory.setItem(playerSection.getInt("slot"), playrItem);
    }

    public static class DashboardHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final Material material;

        @Getter
        private final Shop shop;

        public DashboardHolder(Player player, Shop shop) {
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
