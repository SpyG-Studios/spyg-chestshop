package com.spygstudios.chestshop.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.spygstudios.chestshop.enums.ShopMode;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.item.ItemUtils;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class ShopGui {
    
    // Track per-player shop viewing modes
    private static final Map<UUID, ShopMode> PLAYER_MODES = new HashMap<>();

    public static void open(ChestShop plugin, Player player, Shop shop) {
        // Get or default the player's preferred mode for this shop
        ShopMode mode = getPlayerMode(player, shop);
        open(plugin, player, shop, mode);
    }

    public static void open(ChestShop plugin, Player player, Shop shop, ShopMode mode) {
        // Store the player's current mode preference
        setPlayerMode(player, mode);
        GuiConfig config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new ShopGuiHolder(player, shop), 27, TranslateColor.translate(config.getString("shop.title").replace("%shop-name%", shop.getName())));

        // Mode toggle item (only show if both modes are available and shop has items to buy)
        if (shop.acceptsCustomerPurchases() && shop.acceptsCustomerSales() && shop.getItemsLeft() > 0) {
            Material modeMaterial = mode == ShopMode.CUSTOMER_PURCHASING ?
                Material.getMaterial(config.getString("shop.mode.buying.material", "GREEN_WOOL")) :
                Material.getMaterial(config.getString("shop.mode.selling.material", "RED_WOOL"));
            String modeTitle = mode == ShopMode.CUSTOMER_PURCHASING ? 
                config.getString("shop.mode.buying.title", "&eBuying Mode") : 
                config.getString("shop.mode.selling.title", "&eSelling Mode");
            List<String> modeLore = mode == ShopMode.CUSTOMER_PURCHASING ? 
                config.getStringList("shop.mode.buying.lore") : 
                config.getStringList("shop.mode.selling.lore");
            ItemStack modeItem = ItemUtils.create(modeMaterial, modeTitle, modeLore);
            PersistentData modeData = new PersistentData(plugin, modeItem);
            modeData.set("action", GuiAction.TOGGLE_MODE.name());
            modeData.save();
            inventory.setItem(4, modeItem);
        }

        // Set shop material based on mode
        if ((mode == ShopMode.CUSTOMER_PURCHASING && shop.acceptsCustomerPurchases()) || 
            (mode == ShopMode.CUSTOMER_SELLING && shop.acceptsCustomerSales())) {
            
            ItemStack shopItem = new ItemStack(shop.getMaterial());
            ItemMeta shopMeta = shopItem.getItemMeta();
            
            String titleKey = mode == ShopMode.CUSTOMER_PURCHASING ? "shop.item-to-buy.title" : "shop.item-to-sell.title";
            String loreKey = mode == ShopMode.CUSTOMER_PURCHASING ? "shop.item-to-buy.lore" : "shop.item-to-sell.lore";
            
            shopMeta.displayName(TranslateColor.translate(config.getString(titleKey, "&e%material%").replace("%material%", shop.getMaterial().name())));
            double priceForMode = mode == ShopMode.CUSTOMER_PURCHASING ? shop.getCustomerPurchasePrice() : shop.getCustomerSalePrice();
            List<Component> translatedLore = plugin.getGuiConfig().getStringList(loreKey).stream()
                    .map(line -> TranslateColor.translate(line.replace("%price%", String.valueOf(priceForMode)))).toList();
            shopMeta.lore(translatedLore);
            shopItem.setItemMeta(shopMeta);
            
            PersistentData data = new PersistentData(plugin, shopItem);
            data.set("action", mode == ShopMode.CUSTOMER_PURCHASING ? GuiAction.BUY.name() : GuiAction.SELL.name());
            data.set("mode", mode.name());
            data.save();
            inventory.setItem(13, shopItem);
        }

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
            String title = config.getString("shop.amount.items." + key + ".title")
                    .replace("%amount%", String.valueOf(amount).replace("-", ""));
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

    private static ShopMode getPlayerMode(Player player, Shop shop) {
        // Determine the appropriate customer interaction mode with the shop
        ShopMode mode = PLAYER_MODES.get(player.getUniqueId());
        
        // If shop is empty but accepts customer sales, force customer selling mode
        if (shop.getItemsLeft() == 0 && shop.acceptsCustomerSales()) {
            return ShopMode.CUSTOMER_SELLING;
        }
        
        if (mode == null) {
            // Default to customer purchasing if shop accepts purchases, otherwise customer selling
            if (shop.acceptsCustomerPurchases()) {
                mode = ShopMode.CUSTOMER_PURCHASING;
            } else if (shop.acceptsCustomerSales()) {
                mode = ShopMode.CUSTOMER_SELLING;
            } else {
                mode = ShopMode.CUSTOMER_PURCHASING; // fallback
            }
        }
        // Ensure the mode is valid for the shop's capabilities
        if ((mode == ShopMode.CUSTOMER_SELLING && !shop.acceptsCustomerSales()) || 
            (mode == ShopMode.CUSTOMER_PURCHASING && !shop.acceptsCustomerPurchases())) {
            mode = shop.acceptsCustomerSales() ? ShopMode.CUSTOMER_SELLING : ShopMode.CUSTOMER_PURCHASING;
            // Update the player's mode to the valid one
            setPlayerMode(player, mode);
        }
        return mode;
    }
    
    private static void setPlayerMode(Player player, ShopMode mode) {
        PLAYER_MODES.put(player.getUniqueId(), mode);
    }
    
    public static ShopMode getPlayerMode(Player player) {
        return PLAYER_MODES.getOrDefault(player.getUniqueId(), ShopMode.CUSTOMER_PURCHASING);
    }
    
    public static void clearPlayerMode(Player player) {
        PLAYER_MODES.remove(player.getUniqueId());
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
