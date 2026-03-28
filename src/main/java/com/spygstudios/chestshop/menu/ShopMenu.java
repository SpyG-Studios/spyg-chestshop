package com.spygstudios.chestshop.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.MenuConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.enums.ShopMode;
import com.spygstudios.chestshop.menu.holder.BaseHolder;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.shop.ShopUtils;
import com.spygstudios.chestshop.utils.FormatUtils;
import com.spygstudios.chestshop.utils.PageUtil;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.datacontainer.ItemContainer;
import com.spygstudios.spyglib.item.ItemUtils;

import net.kyori.adventure.text.Component;

public class ShopMenu implements Listener {

    private final ChestShop plugin;
    private final Map<UUID, ShopMode> playerModes = new HashMap<>();
    private final Map<UUID, Long> lastAmountClick = new HashMap<>();

    public ShopMenu(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, Shop shop) {
        open(player, shop, resolvePlayerMode(player, shop));
    }

    public void open(Player player, Shop shop, ShopMode mode) {
        MenuConfig config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(
                new ShopHolder(player, shop), 27,
                TranslateColor.translate(config.getString("shop.title").replace("%shop-name%", shop.getName())));

        if (shop.acceptsCustomerPurchases() && shop.acceptsCustomerSales() && shop.getItemsLeft() > 0) {
            addModeToggle(config, inventory, mode);
        }

        addShopItem(config, shop, inventory, mode);

        if (shop.getAddedPlayers().contains(player.getUniqueId())) {
            addInventoryButton(config, inventory);
        }

        addAmountButtons(config, shop, inventory);
        PageUtil.setFillItems(inventory, "shop");
        player.openInventory(inventory);
    }

    private void addModeToggle(MenuConfig config, Inventory inventory, ShopMode mode) {
        String prefix = mode == ShopMode.CUSTOMER_PURCHASING ? "shop.mode.buying" : "shop.mode.selling";
        Material material = Material.getMaterial(config.getString(prefix + ".material", "EMERALD"));
        String title = config.getString(prefix + ".title");
        List<String> lore = config.getStringList(prefix + ".lore");

        ItemStack item = ItemUtils.create(material, title, lore);
        ItemContainer.create(plugin, item).set("action", GuiAction.TOGGLE_MODE.name());
        inventory.setItem(4, item);
    }

    private void addShopItem(MenuConfig config, Shop shop, Inventory inventory, ShopMode mode) {
        ItemStack shopItem = shop.getItem();
        shopItem.setAmount(shop.getQuantity());
        ItemMeta meta = shopItem.getItemMeta();

        String titleKey = mode == ShopMode.CUSTOMER_PURCHASING ? "shop.item-to-buy.title" : "shop.item-to-sell.title";
        String loreKey = mode == ShopMode.CUSTOMER_PURCHASING ? "shop.item-to-buy.lore" : "shop.item-to-sell.lore";
        double price = mode == ShopMode.CUSTOMER_PURCHASING ? shop.getCustomerPurchasePrice() : shop.getCustomerSalePrice();

        meta.displayName(TranslateColor.translate(config.getString(titleKey, "&e%item%").replace("%item%", shop.getItemName())));

        List<Component> lore = new java.util.ArrayList<>(config.getStringList(loreKey).stream()
                .map(line -> TranslateColor.translate(line.replace("%price%", FormatUtils.formatNumber(price)))).toList());

        if (shopItem.getType().name().contains("SHULKER_BOX")) {
            lore.add(Component.empty());
            lore.add(TranslateColor.translate(Message.SHOP_SHULKER_PREVIEW.getRaw()));
        }

        meta.lore(lore);
        shopItem.setItemMeta(meta);

        ItemContainer data = ItemContainer.create(plugin, shopItem);
        data.set("action", (mode == ShopMode.CUSTOMER_PURCHASING ? GuiAction.BUY : GuiAction.SELL).name());
        data.set("mode", mode.name());
        inventory.setItem(13, shopItem);
    }

    private void addInventoryButton(MenuConfig config, Inventory inventory) {
        Material material = Material.getMaterial(config.getString("chestshop.inventory.material", "CHEST"));
        ItemStack item = ItemUtils.create(material, config.getString("chestshop.inventory.title"), config.getStringList("chestshop.inventory.lore"));
        ItemContainer.create(plugin, item).set("action", GuiAction.OPEN_SHOP_INVENTORY.name());
        inventory.setItem(18, item);
    }

    private void addAmountButtons(MenuConfig config, Shop shop, Inventory inventory) {
        ConfigurationSection section = config.getConfigurationSection("shop.amount.items");
        int quantity = shop.getQuantity();
        int maxStack = shop.getItem().getMaxStackSize();

        section.getKeys(false).forEach(key -> {
            int slot = section.getInt(key + ".slot");
            int amount = section.getInt(key + ".amount");
            int effectiveStep = Math.abs(amount) * quantity;
            String title = section.getString(key + ".title").replace("%amount%", String.valueOf(Math.min(effectiveStep, maxStack)));
            List<String> lore = section.getStringList(key + ".lore");
            Material material = Material.getMaterial(section.getString(key + ".material", "GRAY_STAINED_GLASS_PANE"));
            List<Float> modelFloats = section.getFloatList(key + ".model-data.floats");
            List<String> modelStrings = section.getStringList(key + ".model-data.strings");

            if (maxStack >= Math.abs(amount)) {
                ItemStack item = ItemUtils.create(material, title, lore, modelFloats, modelStrings, Math.min(effectiveStep, maxStack));
                ItemContainer data = ItemContainer.create(plugin, item);
                data.set("action", GuiAction.SET_ITEM_AMOUNT.name());
                data.set("amount", amount);
                inventory.setItem(slot, item);
            }
        });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopHolder holder)) {
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

        GuiAction action = GuiAction.valueOf(actionStr);

        switch (action) {
            case SET_ITEM_AMOUNT -> handleAmountChange(event, holder, data);
            case BUY -> handleBuy(event, holder);
            case SELL -> handleSell(event, holder);
            case TOGGLE_MODE -> handleModeToggle(holder);
            case OPEN_SHOP_INVENTORY -> holder.getShop().openShopInventory(holder.getPlayer());
            default -> {
            }
        }
    }

    private void handleAmountChange(InventoryClickEvent event, ShopHolder holder, ItemContainer data) {
        if (System.currentTimeMillis() - getLastClick(event.getWhoClicked()) < 100) {
            return;
        }
        lastAmountClick.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());

        ItemStack item = event.getInventory().getItem(13);
        Player player = holder.getPlayer();
        Shop shop = holder.getShop();
        ShopMode mode = getPlayerMode(player);

        int quantity = shop.getQuantity();
        int max;
        if (mode == ShopMode.CUSTOMER_PURCHASING) {
            max = Math.min(item.getMaxStackSize(), shop.getItemsLeft());
        } else {
            max = Math.min(item.getMaxStackSize(), ShopUtils.getSellableItemCount(player.getInventory(), shop.getItem()));
        }
        int min = quantity;
        max = Math.max(min, (max / quantity) * quantity);

        int modifier = data.getInt("amount") * quantity;
        int newAmount = Math.max(min, Math.min(max, item.getAmount() + modifier));
        item.setAmount(newAmount);

        String loreKey = mode == ShopMode.CUSTOMER_PURCHASING ? "shop.item-to-buy.lore" : "shop.item-to-sell.lore";
        double price = mode == ShopMode.CUSTOMER_PURCHASING ? shop.getCustomerPurchasePrice() : shop.getCustomerSalePrice();
        List<Component> lore = plugin.getGuiConfig().getStringList(loreKey).stream()
                .map(line -> TranslateColor.translate(line.replace("%price%", String.valueOf((newAmount / quantity) * price)))).toList();

        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private void handleBuy(InventoryClickEvent event, ShopHolder holder) {
        if (event.getClick() == ClickType.RIGHT) {
            openShulkerPreview(holder.getPlayer(), holder.getShop().getItem());
            return;
        }
        ItemStack item = event.getInventory().getItem(13);
        holder.getShop().getShopTransactions().sell(holder.getPlayer(), item.getAmount());
        if (holder.getShop().getItemsLeft() == 0) {
            holder.getShop().getHologram().updateHologramRows();
            holder.getPlayer().closeInventory();
            Message.SHOP_EMPTY.send(holder.getPlayer());
        }
    }

    private void handleSell(InventoryClickEvent event, ShopHolder holder) {
        if (event.getClick() == ClickType.RIGHT) {
            openShulkerPreview(holder.getPlayer(), holder.getShop().getItem());
            return;
        }
        ItemStack item = event.getInventory().getItem(13);
        holder.getShop().getShopTransactions().buy(holder.getPlayer(), item.getAmount());
    }

    private void handleModeToggle(ShopHolder holder) {
        Player player = holder.getPlayer();
        Shop shop = holder.getShop();
        ShopMode current = getPlayerMode(player);
        ShopMode newMode = current == ShopMode.CUSTOMER_PURCHASING ? ShopMode.CUSTOMER_SELLING : ShopMode.CUSTOMER_PURCHASING;

        if ((newMode == ShopMode.CUSTOMER_PURCHASING && !shop.acceptsCustomerPurchases())
                || (newMode == ShopMode.CUSTOMER_SELLING && !shop.acceptsCustomerSales())) {
            return;
        }

        setPlayerMode(player, newMode);
        open(player, shop, newMode);
    }

    private void openShulkerPreview(Player player, ItemStack item) {
        if (item == null || !item.getType().name().contains("SHULKER_BOX")) {
            return;
        }
        if (item.getItemMeta() instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof ShulkerBox shulker) {
            Inventory preview = plugin.getServer().createInventory(
                    new ShulkerPreviewHolder(), 27,
                    TranslateColor.translate(Message.SHOP_SHULKER_PREVIEW_TITLE.getRaw()));
            preview.setContents(shulker.getInventory().getContents());
            player.openInventory(preview);
        }
    }

    @EventHandler
    public void onShulkerPreviewClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ShulkerPreviewHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShulkerPreviewDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ShulkerPreviewHolder) {
            event.setCancelled(true);
        }
    }

    private ShopMode resolvePlayerMode(Player player, Shop shop) {
        UUID uuid = player.getUniqueId();
        boolean canBuy = shop.acceptsCustomerPurchases();
        boolean canSell = shop.acceptsCustomerSales();
        boolean hasItems = shop.getItemsLeft() > 0;

        if (!hasItems && canSell) {
            playerModes.put(uuid, ShopMode.CUSTOMER_SELLING);
            return ShopMode.CUSTOMER_SELLING;
        }

        ShopMode mode = playerModes.get(uuid);
        if (mode == null) {
            mode = canBuy ? ShopMode.CUSTOMER_PURCHASING : ShopMode.CUSTOMER_SELLING;
        } else if (mode == ShopMode.CUSTOMER_PURCHASING && !canBuy) {
            mode = ShopMode.CUSTOMER_SELLING;
        } else if (mode == ShopMode.CUSTOMER_SELLING && !canSell) {
            mode = ShopMode.CUSTOMER_PURCHASING;
        }

        playerModes.put(uuid, mode);
        return mode;
    }

    public ShopMode getPlayerMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), ShopMode.CUSTOMER_PURCHASING);
    }

    public void setPlayerMode(Player player, ShopMode mode) {
        playerModes.put(player.getUniqueId(), mode);
    }

    public void clearPlayerMode(Player player) {
        playerModes.remove(player.getUniqueId());
    }

    private long getLastClick(HumanEntity player) {
        return lastAmountClick.getOrDefault(player.getUniqueId(), 0L);
    }

    public static class ShopHolder extends BaseHolder {
        public ShopHolder(Player player, Shop shop) {
            super(player, shop);
        }
    }

    public static class ShulkerPreviewHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
