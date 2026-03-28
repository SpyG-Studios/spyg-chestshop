package com.spygstudios.chestshop.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.MenuConfig;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.menu.holder.BaseHolder;
import com.spygstudios.chestshop.shop.AmountHandler;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.chestshop.utils.FormatUtils;
import com.spygstudios.chestshop.utils.PageUtil;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.datacontainer.ItemContainer;
import com.spygstudios.spyglib.item.ItemUtils;
import com.spygstudios.spyglib.item.PlayerHeads;
import com.spygstudios.spyglib.placeholder.ParseListPlaceholder;

import lombok.Getter;

public class DashboardMenu implements Listener {

    private final ChestShop plugin;

    public DashboardMenu(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, Shop shop) {
        MenuConfig guiConfig = plugin.getGuiConfig();
        Inventory inventory = Bukkit.createInventory(
                new DashboardHolder(player, shop), 27,
                TranslateColor.translate(
                        guiConfig.getString("chestshop.title")
                                .replace("%shop-name%", shop.getName())
                                .replace("%player-name%", Bukkit.getOfflinePlayer(shop.getOwnerId()).getName())));

        buildInventory(guiConfig, shop, inventory);
        PageUtil.setFillItems(inventory, "chestshop");
        player.openInventory(inventory);
    }

    private void buildInventory(MenuConfig guiConfig, Shop shop, Inventory inventory) {
        Config config = plugin.getConf();
        setMainItem(guiConfig, shop, inventory);
        setInfoItem(guiConfig, config, shop, inventory);
        setNotifyItem(guiConfig, shop, inventory);
        setMoneyItem(guiConfig, shop, inventory);
        setInventoryItem(guiConfig, inventory);
        setQuantityItem(guiConfig, shop, inventory);
        setBuySellToggleItem(guiConfig, shop, inventory);
        setPlayerItem(guiConfig, shop, inventory);
    }

    private void setMainItem(MenuConfig guiConfig, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.item");
        ItemStack item = shop.getItem() != null
                ? shop.getItem()
                : ItemUtils.create(
                        Material.getMaterial(section.getString("not-set", "BARRIER")),
                        section.getString("title"),
                        section.getStringList("lore"),
                        section.getFloatList("model-data.floats"),
                        section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.SET_ITEM.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private void setInfoItem(MenuConfig guiConfig, Config config, Shop shop, Inventory inventory) {
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

        ItemStack item = ItemUtils.create(
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

        inventory.setItem(section.getInt("slot"), item);
    }

    private void setNotifyItem(MenuConfig guiConfig, Shop shop, Inventory inventory) {
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

    private void setMoneyItem(MenuConfig guiConfig, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.money");
        Material material = Material.getMaterial(section.getString("material", "GOLD_INGOT"));
        if (material.equals(Material.AIR)) {
            return;
        }
        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(line.replace("%sell-price%", FormatUtils.formatNumber(shop.getCustomerPurchasePrice()))
                    .replace("%buy-price%", FormatUtils.formatNumber(shop.getCustomerSalePrice())));
        }

        ItemStack item = ItemUtils.create(
                material, section.getString("title"), lore,
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.SET_SHOP_BUY_PRICE.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private void setInventoryItem(MenuConfig guiConfig, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.inventory");
        Material material = Material.getMaterial(section.getString("material", "CHEST"));
        if (material.equals(Material.AIR)) {
            return;
        }
        ItemStack item = ItemUtils.create(
                material, section.getString("title"), section.getStringList("lore"),
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private void setQuantityItem(MenuConfig guiConfig, Shop shop, Inventory inventory) {
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
                material, section.getString("title"), lore,
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.SET_SHOP_QUANTITY.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private void setBuySellToggleItem(MenuConfig guiConfig, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.buysell");
        Material material = Material.getMaterial(section.getString("material", "LEVER"));
        if (material.equals(Material.AIR)) {
            return;
        }

        ItemStack item = ItemUtils.create(
                material, section.getString("title"), buildBuySellLore(guiConfig, shop),
                section.getFloatList("model-data.floats"),
                section.getStringList("model-data.strings"));

        ItemContainer.create(plugin, item).set("action", GuiAction.TOGGLE_SELLING.name());
        inventory.setItem(section.getInt("slot"), item);
    }

    private void setPlayerItem(MenuConfig guiConfig, Shop shop, Inventory inventory) {
        ConfigurationSection section = guiConfig.getConfigurationSection("chestshop.player");
        Material material = Material.getMaterial(section.getString("material", "PLAYER_HEAD"));
        if (material.equals(Material.AIR)) {
            return;
        }

        ItemStack item;
        if (material.equals(Material.PLAYER_HEAD)) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
            item = owner.isOnline()
                    ? PlayerHeads.getOnlinePlayerHead(owner.getUniqueId())
                    : PlayerHeads.getOfflinePlayerHead(owner.getUniqueId());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(TranslateColor.translate(section.getString("title").replace("%player-name%", owner.getName())));
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

    private List<String> buildBuySellLore(MenuConfig guiConfig, Shop shop) {
        String sellStatus = shop.acceptsCustomerPurchases()
                ? guiConfig.getString("chestshop.buysell.sell.enabled", "&aEnabled")
                : guiConfig.getString("chestshop.buysell.sell.disabled", "&cDisabled");
        String buyStatus = shop.acceptsCustomerSales()
                ? guiConfig.getString("chestshop.buysell.buy.enabled", "&aEnabled")
                : guiConfig.getString("chestshop.buysell.buy.disabled", "&cDisabled");

        List<String> lore = new ArrayList<>();
        lore.add(guiConfig.getString("chestshop.buysell.sell.line").replace("%status%", sellStatus));
        lore.add(guiConfig.getString("chestshop.buysell.buy.line").replace("%status%", buyStatus));
        lore.addAll(guiConfig.getStringList("chestshop.buysell.lore"));
        return lore;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof DashboardHolder holder)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        ItemContainer data = ItemContainer.create(plugin, clickedItem);
        String actionStr = data.getString("action");
        if (actionStr == null) {
            return;
        }

        Player player = holder.getPlayer();
        Shop shop = holder.getShop();
        GuiAction action = GuiAction.valueOf(actionStr);

        switch (action) {
            case SET_ITEM -> handleSetItem(event);
            case TOGGLE_NOTIFY -> handleToggleNotify(shop, clickedItem, player);
            case SET_SHOP_BUY_PRICE -> handlePriceSetting(event, player, shop);
            case SET_SHOP_SELL_PRICE, SET_SHOP_QUANTITY -> {
                cancelPendingAmountHandler(player);
                new AmountHandler(player, shop, action);
                event.getInventory().close();
            }
            case TOGGLE_SELLING -> handleBuySellToggle(event, shop, player);
            case OPEN_PLAYERS -> plugin.getPlayersGui().open(player, shop);
            case OPEN_SHOP_INVENTORY -> shop.openShopInventory(player);
            case CLOSE -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleSetItem(InventoryClickEvent event) {
        if (event.getCursor() == null || event.getCursor().getType().isAir()) {
            return;
        }
        ItemStack item = event.getCursor().clone();
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(meta);
        }
        event.getInventory().setItem(13, item);
        ItemContainer.create(plugin, event.getInventory().getItem(13)).set("action", GuiAction.SET_ITEM.name());
    }

    private void handleToggleNotify(Shop shop, ItemStack clickedItem, Player player) {
        shop.setNotify(!shop.isNotify());
        ItemMeta meta = clickedItem.getItemMeta();
        meta.lore(Arrays.asList(TranslateColor.translate(shop.isNotify()
                ? plugin.getGuiConfig().getString("chestshop.notify.on")
                : plugin.getGuiConfig().getString("chestshop.notify.off"))));
        clickedItem.setItemMeta(meta);
        player.updateInventory();
    }

    private void handlePriceSetting(InventoryClickEvent event, Player player, Shop shop) {
        GuiAction priceAction = event.getClick().isLeftClick()
                ? GuiAction.SET_SHOP_SELL_PRICE
                : GuiAction.SET_SHOP_BUY_PRICE;
        cancelPendingAmountHandler(player);
        new AmountHandler(player, shop, priceAction);
        event.getInventory().close();
    }

    private void handleBuySellToggle(InventoryClickEvent event, Shop shop, Player player) {
        if (!shop.acceptsCustomerPurchases() && !shop.acceptsCustomerSales()) {
            shop.setCanSellToPlayers(true);
            shop.setCanBuyFromPlayers(true);
        } else if (!shop.acceptsCustomerPurchases() && shop.acceptsCustomerSales()) {
            shop.setCanSellToPlayers(false);
            shop.setCanBuyFromPlayers(false);
        } else if (shop.acceptsCustomerPurchases() && shop.acceptsCustomerSales()) {
            shop.setCanSellToPlayers(true);
            shop.setCanBuyFromPlayers(false);
        } else {
            shop.setCanSellToPlayers(false);
            shop.setCanBuyFromPlayers(true);
        }

        ItemStack item = event.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        meta.lore(TranslateColor.translate(buildBuySellLore(plugin.getGuiConfig(), shop)));
        item.setItemMeta(meta);
        player.updateInventory();
    }

    private void cancelPendingAmountHandler(Player player) {
        AmountHandler pending = AmountHandler.getPendingAmount(player);
        if (pending != null) {
            pending.cancel();
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof DashboardHolder holder)) {
            return;
        }
        saveItemIfChanged(event.getInventory(), holder);
    }

    private void saveItemIfChanged(Inventory inventory, DashboardHolder holder) {
        ItemStack item = inventory.getItem(13);
        Shop shop = holder.getShop();
        if (item == null || ShopUtils.isSimilar(item, shop.getItem())) {
            return;
        }
        ItemContainer newData = ItemContainer.create(plugin, item);
        newData.remove("action");
        shop.setShopItem(item);
        shop.getHologram().updateHologramRows();
    }

    @Getter
    public static class DashboardHolder extends BaseHolder {
        private final ItemStack item;

        public DashboardHolder(Player player, Shop shop) {
            super(player, shop);
            this.item = shop.getItem() == null ? new ItemStack(Material.AIR) : shop.getItem();
        }
    }
}
