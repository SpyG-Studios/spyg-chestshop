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
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.utils.FormatUtils;
import com.spygstudios.chestshop.utils.PageUtil;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.datacontainer.ItemContainer;
import com.spygstudios.spyglib.item.ItemUtils;
import com.spygstudios.spyglib.item.PlayerHeads;
import com.spygstudios.spyglib.placeholder.ParseListPlaceholder;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DashboardGui {

    private static GuiConfig guiConfig;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        guiConfig = plugin.getGuiConfig();

        Inventory inventory = Bukkit.createInventory(
                new DashboardHolder(player, shop),
                27,
                TranslateColor.translate(
                        guiConfig.getString("chestshop.title")
                                .replace("%shop-name%", shop.getName())
                                .replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));

        setShopItems(plugin, shop, inventory);
        PageUtil.setFillItems(inventory, "chestshop");
        player.openInventory(inventory);
    }

    private static void setShopItems(ChestShop plugin, Shop shop, Inventory inventory) {
        Config config = plugin.getConf();

        setMainItem(plugin, shop, inventory);
        setInfoItem(config, shop, inventory);
        setNotifyItem(plugin, shop, inventory);
        setMoneyItem(plugin, shop, inventory);
        setInventoryItem(plugin, inventory);
        setQuantityItem(plugin, shop, inventory);
        setBuySellToggleItem(plugin, shop, inventory);
        setPlayerItem(plugin, shop, inventory);
    }

    private static void setMainItem(ChestShop plugin, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.item");

        ItemStack item = shop.getItem() != null
                ? shop.getItem()
                : ItemUtils.create(
                        Material.getMaterial(section.getString("not-set", "BARRIER")),
                        section.getString("title"),
                        section.getStringList("lore"),
                        section.getFloatList("model-data.floats"),
                        section.getStringList("model-data.strings"));

        inventory.setItem(section.getInt("slot"), item);
        ItemContainer.create(plugin, item).set("action", GuiAction.SET_ITEM.name());
    }

    private static void setInfoItem(Config config, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.info");
        Material material = Material.getMaterial(section.getString("material", "WRITABLE_BOOK"));
        if (material.equals(Material.AIR)) {
            return;
        }

        String buyPrice = config.getString("shops.price-format.buy")
                .replace("%quantity%", FormatUtils.formatNumber(shop.getQuantity()))
                .replace("%price%", FormatUtils.formatNumber(shop.getCustomerPurchasePrice()));

        String sellPrice = config.getString("shops.price-format.sell")
                .replace("%quantity%", FormatUtils.formatNumber(shop.getQuantity()))
                .replace("%price%", FormatUtils.formatNumber(shop.getCustomerSalePrice()));

        String priceDisplay;
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

        ItemStack infoItem = ItemUtils.create(
                material,
                section.getString("title"),
                ParseListPlaceholder.parse(
                        section.getStringList("lore"),
                        Map.of(
                                "%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName(),
                                "%item%", shop.getItem() == null ? "AIR" : shop.getItemName(),
                                "%price%", priceDisplay,
                                "%created%", shop.getCreatedAt(),
                                "%location%", shop.getChestLocationString(),
                                "%sold-items%", String.valueOf(shop.getSoldItems()),
                                "%money-earned%", FormatUtils.formatNumber(shop.getMoneyEarned()),
                                "%bought-items%", String.valueOf(shop.getBoughtItems()),
                                "%money-spent%", FormatUtils.formatNumber(shop.getMoneySpent()))),
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        inventory.setItem(section.getInt("slot"), infoItem);
    }

    private static void setNotifyItem(ChestShop plugin, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.notify");
        Material material = Material.getMaterial(section.getString("material", "BELL"));
        if (material.equals(Material.AIR)) {
            return;
        }
        ItemStack item = ItemUtils.create(
                material,
                section.getString("title"),
                Arrays.asList(shop.isNotify() ? section.getString("on") : section.getString("off")),
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.TOGGLE_NOTIFY.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private static void setMoneyItem(ChestShop plugin, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.money");
        Material material = Material.getMaterial(section.getString("material", "GOLD_INGOT"));
        if (material.equals(Material.AIR)) {
            return;
        }
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(
                    line.replace("%sell-price%", FormatUtils.formatNumber(shop.getCustomerPurchasePrice()))
                            .replace("%buy-price%", FormatUtils.formatNumber(shop.getCustomerSalePrice())));
        }

        ItemStack item = ItemUtils.create(
                material,
                section.getString("title"),
                lore,
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.SET_SHOP_BUY_PRICE.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private static void setInventoryItem(ChestShop plugin, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.inventory");
        Material material = Material.getMaterial(section.getString("material", "CHEST"));
        if (material.equals(Material.AIR)) {
            return;
        }
        ItemStack item = ItemUtils.create(
                material,
                section.getString("title"),
                section.getStringList("lore"),
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private static void setQuantityItem(ChestShop plugin, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.quantity");
        Material material = Material.getMaterial(section.getString("material", "HOPPER"));
        if (material.equals(Material.AIR)) {
            return;
        }
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(line.replace("%quantity%", String.valueOf(shop.getQuantity())));
        }

        ItemStack item = ItemUtils.create(
                material,
                section.getString("title"),
                lore,
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.SET_SHOP_QUANTITY.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private static void setBuySellToggleItem(ChestShop plugin, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.buysell");
        Material material = Material.getMaterial(section.getString("material", "LEVER"));
        if (material.equals(Material.AIR)) {
            return;
        }
        String sellStatus = shop.acceptsCustomerPurchases()
                ? section.getString("sell.enabled", "&aEnabled")
                : section.getString("sell.disabled", "&cDisabled");

        String buyStatus = shop.acceptsCustomerSales()
                ? section.getString("buy.enabled", "&aEnabled")
                : section.getString("buy.disabled", "&cDisabled");

        List<String> lore = new ArrayList<>();
        lore.add(section.getString("sell.line").replace("%status%", sellStatus));
        lore.add(section.getString("buy.line").replace("%status%", buyStatus));
        lore.addAll(section.getStringList("lore"));

        ItemStack item = ItemUtils.create(
                material,
                section.getString("title"),
                lore,
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.TOGGLE_SELLING.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private static void setPlayerItem(ChestShop plugin, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.player");
        Material material = Material.getMaterial(section.getString("material", "PLAYER_HEAD"));
        if (material.equals(Material.AIR)) {
            return;
        }
        ItemStack item = null;
        if (material.equals(Material.PLAYER_HEAD)) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
            item = owner.isOnline()
                    ? PlayerHeads.getOnlinePlayerHead(owner.getUniqueId())
                    : PlayerHeads.getOfflinePlayerHead(owner.getUniqueId());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(
                    TranslateColor.translate(section.getString("title")
                            .replace("%player-name%", owner.getName())));
            meta.lore(TranslateColor.translate(section.getStringList("lore")));
            item.setItemMeta(meta);
        } else {
            item = ItemUtils.create(
                    material,
                    section.getString("title").replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName()),
                    section.getStringList("lore"),
                    section.getFloatList("model-data.floats"),
                    section.getStringList("model-data.strings"));
        }

        ItemContainer.create(plugin, item).set("action", GuiAction.OPEN_PLAYERS.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    public static class DashboardHolder implements InventoryHolder {

    @Getter
    public static class DashboardHolder extends BaseHolder {
        private final ItemStack item;

        public DashboardHolder(Player player, Shop shop) {
            super(player, shop);
            this.item = shop.getItem() == null ? new ItemStack(Material.AIR) : shop.getItem();
        }
    }
}
