package com.spygstudios.chestshop.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.PageUtil;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.enums.ShopMode;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.item.ItemUtils;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class ShopGui {

    private static final Map<UUID, ShopMode> PLAYER_MODES = new HashMap<>();

    public static void open(ChestShop plugin, Player player, Shop shop) {
        ShopMode mode = getPlayerMode(player, shop);
        open(plugin, player, shop, mode);
    }

    public static void open(ChestShop plugin, Player player, Shop shop, ShopMode mode) {
        GuiConfig config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new ShopHolder(player, shop), 27, TranslateColor.translate(config.getString("shop.title").replace("%shop-name%", shop.getName())));

        if (shop.acceptsCustomerPurchases() && shop.acceptsCustomerSales() && shop.getItemsLeft() > 0) {
            Material modeMaterial = mode == ShopMode.CUSTOMER_PURCHASING
                    ? Material.getMaterial(config.getString("shop.mode.buying.material", "GREEN_WOOL"))
                    : Material.getMaterial(config.getString("shop.mode.selling.material", "RED_WOOL"));
            String modeTitle = mode == ShopMode.CUSTOMER_PURCHASING
                    ? config.getString("shop.mode.buying.title", "&eBuying Mode")
                    : config.getString("shop.mode.selling.title", "&eSelling Mode");
            List<String> modeLore = mode == ShopMode.CUSTOMER_PURCHASING
                    ? config.getStringList("shop.mode.buying.lore")
                    : config.getStringList("shop.mode.selling.lore");
            ItemStack modeItem = ItemUtils.create(modeMaterial, modeTitle, modeLore);
            PersistentData modeData = new PersistentData(plugin, modeItem);
            modeData.set("action", GuiAction.TOGGLE_MODE.name());
            inventory.setItem(4, modeItem);
        }

        ItemStack shopItem = new ItemStack(shop.getMaterial());
        ItemMeta shopMeta = shopItem.getItemMeta();

        String titleKey = mode == ShopMode.CUSTOMER_PURCHASING
                ? "shop.item-to-buy.title"
                : "shop.item-to-sell.title";
        String loreKey = mode == ShopMode.CUSTOMER_PURCHASING
                ? "shop.item-to-buy.lore"
                : "shop.item-to-sell.lore";

        shopMeta.displayName(TranslateColor.translate(config.getString(titleKey, "&e%material%").replace("%material%", shop.getMaterial().name())));
        double priceForMode = mode == ShopMode.CUSTOMER_PURCHASING
                ? shop.getCustomerPurchasePrice()
                : shop.getCustomerSalePrice();
        List<Component> translatedLore = plugin.getGuiConfig().getStringList(loreKey).stream()
                .map(line -> TranslateColor.translate(line.replace("%price%", String.valueOf(priceForMode)))).toList();
        shopMeta.lore(translatedLore);
        shopItem.setItemMeta(shopMeta);

        PersistentData data = new PersistentData(plugin, shopItem);
        data.set("action", mode == ShopMode.CUSTOMER_PURCHASING
                ? GuiAction.BUY.name()
                : GuiAction.SELL.name());
        data.set("mode", mode.name());
        inventory.setItem(13, shopItem);

        if (shop.getAddedPlayers().contains(player.getUniqueId())) {
            Material inventoryMaterial = Material.getMaterial(config.getString("chestshop.inventory.material", "CHEST"));
            ItemStack inventoryItem = ItemUtils.create(inventoryMaterial, config.getString("chestshop.inventory.title"), config.getStringList("chestshop.inventory.lore"));
            PersistentData inventoryData = new PersistentData(plugin, inventoryItem);
            inventoryData.set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
            inventory.setItem(18, inventoryItem);
        }

        ConfigurationSection amountSection = config.getConfigurationSection("shop.amount.items");
        amountSection.getKeys(false).forEach(key -> {
            int slot = amountSection.getInt(key + ".slot");
            int amount = amountSection.getInt(key + ".amount");
            String title = amountSection.getString(key + ".title").replace("%amount%", String.valueOf(amount).replace("-", ""));
            List<String> lore = amountSection.getStringList(key + ".lore");
            Material material = Material.getMaterial(amountSection.getString(key + ".material", "GRAY_STAINED_GLASS_PANE"));
            List<Float> modelFloats = amountSection.getFloatList(key + ".model-data.floats");
            List<String> modelStrings = amountSection.getStringList(key + ".model-data.strings");
            addItemToInventory(plugin, inventory, slot, material, title, lore, modelFloats, modelStrings, amount);
        });

        PageUtil.setFillItems(inventory, "shop");
        player.openInventory(inventory);

    }

    private void addItemToInventory(ChestShop plugin, Inventory inventory, int slot, Material material, String title, List<String> lore, List<Float> modelFloats, List<String> modelStrings,
            int amount) {
        ItemStack item = ItemUtils.create(material, title, lore, modelFloats, modelStrings, Math.abs(amount));
        PersistentData data = new PersistentData(plugin, item);
        data.set("action", GuiAction.SET_ITEM_AMOUNT.name());
        data.set("amount", amount);
        inventory.setItem(slot, item);
    }

    private static ShopMode getPlayerMode(Player player, Shop shop) {
        ShopMode mode = PLAYER_MODES.get(player.getUniqueId());

        if (mode == null) {
            if (shop.acceptsCustomerPurchases()) {
                mode = ShopMode.CUSTOMER_PURCHASING;
            } else if (shop.acceptsCustomerSales()) {
                mode = ShopMode.CUSTOMER_SELLING;
            }
        }

        if (shop.getItemsLeft() == 0 && shop.acceptsCustomerSales()) {
            return ShopMode.CUSTOMER_SELLING;
        }

        if ((mode == ShopMode.CUSTOMER_SELLING && !shop.acceptsCustomerSales()) ||
                (mode == ShopMode.CUSTOMER_PURCHASING && !shop.acceptsCustomerPurchases())) {
            mode = shop.acceptsCustomerSales()
                    ? ShopMode.CUSTOMER_SELLING
                    : ShopMode.CUSTOMER_PURCHASING;
        }
        return mode;
    }

    public static void setPlayerMode(Player player, ShopMode mode) {
        PLAYER_MODES.put(player.getUniqueId(), mode);
    }

    public static ShopMode getPlayerMode(Player player) {
        return PLAYER_MODES.getOrDefault(player.getUniqueId(), ShopMode.CUSTOMER_PURCHASING);
    }

    public static void clearPlayerMode(Player player) {
        PLAYER_MODES.remove(player.getUniqueId());
    }

    public static class ShopHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final Shop shop;

        public ShopHolder(Player player, Shop shop) {
            this.player = player;
            this.shop = shop;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
