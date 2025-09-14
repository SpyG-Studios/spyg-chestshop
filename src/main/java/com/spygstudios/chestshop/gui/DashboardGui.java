package com.spygstudios.chestshop.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
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
        ConfigurationSection guiMaterialSection = guiConfig.getConfigurationSection("chestshop.material");
        ItemStack shopMaterial = shop.getMaterial() != null
                ? new ItemStack(shop.getMaterial())
                : ItemUtils.create(
                        Material.getMaterial(guiMaterialSection.getString("not-set.material", "BARRIER")),
                        guiMaterialSection.getString("title"),
                        guiMaterialSection.getStringList("lore"),
                        guiMaterialSection.getFloatList("model-data.floats"),
                        guiMaterialSection.getStringList("model-data.strings"));
        inventory.setItem(guiMaterialSection.getInt("slot"), shopMaterial);
        PersistentData materialData = new PersistentData(plugin, shopMaterial);
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

        ConfigurationSection infoSection = guiConfig.getConfigurationSection("chestshop.info");
        ItemStack infoItem = ItemUtils.create(
                infoMaterial,
                infoSection.getString("title"),
                ParseListPlaceholder.parse(infoSection.getStringList("lore"), Map.of(
                        "%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName(),
                        "%material%", shop.getMaterial() == null ? "AIR" : shop.getMaterial().name(),
                        "%price%", priceDisplay,
                        "%created%", shop.getCreatedAt(),
                        "%location%", shop.getChestLocationString(),
                        "%sold-items%", String.valueOf(shop.getSoldItems()),
                        "%money-earned%", String.valueOf(shop.getMoneyEarned()))),
                infoSection.getFloatList("model-data.floats"),
                infoSection.getStringList("model-data.strings"));

        inventory.setItem(infoSection.getInt("slot"), infoItem);

        // notify item
        ConfigurationSection notifySection = guiConfig.getConfigurationSection("chestshop.notify");
        Material notifyMaterial = Material.getMaterial(notifySection.getString("material", "BELL"));
        ItemStack notifyItem = ItemUtils.create(
                notifyMaterial,
                notifySection.getString("title"),
                Arrays.asList(shop.isNotify()
                        ? notifySection.getString("on")
                        : notifySection.getString("off")),
                notifySection.getFloatList("model-data.floats"),
                notifySection.getStringList("model-data.strings"));
        PersistentData notifyData = new PersistentData(plugin, notifyItem);
        notifyData.set("action", GuiAction.TOGGLE_NOTIFY.name());
        inventory.setItem(notifySection.getInt("slot"), notifyItem);

        // money item
        ConfigurationSection moneySection = guiConfig.getConfigurationSection("chestshop.money");
        Material moneyMaterial = Material.getMaterial(moneySection.getString("material", "GOLD_INGOT"));
        List<String> moneyLore = new ArrayList<>();
        for (String string : moneySection.getStringList("lore")) {
            moneyLore.add(string
                    .replace("%sell-price%", String.valueOf(shop.getCustomerPurchasePrice()))
                    .replace("%buy-price%", String.valueOf(shop.getCustomerSalePrice())));
        }

        ItemStack moneyItem = ItemUtils.create(
                moneyMaterial,
                moneySection.getString("title"),
                moneyLore,
                moneySection.getFloatList("model-data.floats"),
                moneySection.getStringList("model-data.strings"));
        PersistentData moneyData = new PersistentData(plugin, moneyItem);
        moneyData.set("action", GuiAction.SET_SHOP_BUY_PRICE.name());
        inventory.setItem(moneySection.getInt("slot"), moneyItem);

        // inventory item
        ConfigurationSection inventorySection = guiConfig.getConfigurationSection("chestshop.inventory");
        Material inventoryMaterial = Material.getMaterial(inventorySection.getString("material", "CHEST"));
        ItemStack inventoryItem = ItemUtils.create(
                inventoryMaterial,
                inventorySection.getString("title"),
                inventorySection.getStringList("lore"),
                inventorySection.getFloatList("model-data.floats"),
                inventorySection.getStringList("model-data.strings"));
        PersistentData inventoryData = new PersistentData(plugin, inventoryItem);
        inventoryData.set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
        inventory.setItem(inventorySection.getInt("slot"), inventoryItem);

        // buy/sell toggle item
        ConfigurationSection buySellSection = guiConfig.getConfigurationSection("chestshop.buysell");
        Material buySellMaterial = Material.getMaterial(buySellSection.getString("material", "COMPARATOR"));
        List<String> buySellLore = new ArrayList<>();
        String sellStatus = shop.acceptsCustomerPurchases()
                ? buySellSection.getString("sell.enabled", "&aEnabled")
                : buySellSection.getString("sell.disabled", "&cDisabled");
        String buyStatus = shop.acceptsCustomerSales()
                ? buySellSection.getString("buy.enabled", "&aEnabled")
                : buySellSection.getString("buy.disabled", "&cDisabled");
        buySellLore.add(buySellSection.getString("sell.line", "&7Selling: %status%").replace("%status%", sellStatus));
        buySellLore.add(buySellSection.getString("buy.line", "&7Buying: %status%").replace("%status%", buyStatus));
        buySellLore.addAll(buySellSection.getStringList("lore"));
        ItemStack buySellItem = ItemUtils.create(
                buySellMaterial,
                buySellSection.getString("title"),
                buySellLore,
                buySellSection.getFloatList("model-data.floats"),
                buySellSection.getStringList("model-data.strings"));
        PersistentData buySellData = new PersistentData(plugin, buySellItem);
        buySellData.set("action", GuiAction.TOGGLE_SELLING.name());
        inventory.setItem(buySellSection.getInt("slot"), buySellItem);

        // player item
        ConfigurationSection playerSection = guiConfig.getConfigurationSection("chestshop.player");
        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
        ItemStack playrItem = owner.isOnline() ? PlayerHeads.getOnlinePlayerHead(owner.getUniqueId()) : PlayerHeads.getOfflinePlayerHead(owner.getUniqueId());
        ItemMeta playrMeta = playrItem.getItemMeta();
        playrMeta.displayName(TranslateColor.translate(playerSection.getString("title").replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));
        playrMeta.lore(TranslateColor.translate(playerSection.getStringList("lore")));
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
