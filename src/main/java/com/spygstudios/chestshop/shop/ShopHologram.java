package com.spygstudios.chestshop.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.utils.FormatUtils;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.hologram.Hologram;
import com.spygstudios.spyglib.hologram.HologramItemRow;
import com.spygstudios.spyglib.hologram.HologramRow;
import com.spygstudios.spyglib.hologram.HologramTextRow;

import lombok.Getter;
import net.kyori.adventure.text.Component;

public class ShopHologram {

    @Getter
    private final Hologram hologram;
    private final Shop shop;
    private final Config config;
    private final ChestShop plugin;

    public ShopHologram(Shop shop, ChestShop plugin) {
        this.shop = shop;
        this.config = plugin.getConf();
        this.plugin = plugin;
        int hologramRange = config.getInt("shops.holograms.range");
        boolean seeTroughWalls = config.getBoolean("shops.holograms.see-through-walls");
        this.hologram = plugin.getHologramManager().createHologram(
                shop.getChestLocation().clone().add(0.5, 0.7, 0.5),
                seeTroughWalls,
                hologramRange);
        updateHologramRows();
    }

    public void updateHologramRows() {
        hologram.setViewDistance(config.getInt("shops.holograms.range"));
        hologram.setSeeTrough(config.getBoolean("shops.holograms.see-through-walls"));
        String owner = Bukkit.getOfflinePlayer(shop.getOwnerId()).getName();
        int currentRowCount = hologram.getRows().size();
        int newRowCount = config.getStringList("shops.lines").size() + 1; // +1 for item row

        int itemIndex = -1;
        if (currentRowCount == newRowCount) {
            int i = 0;
            for (HologramRow row : hologram.getRows()) {
                if (!(row instanceof HologramTextRow)) {
                    if (row instanceof HologramItemRow) {
                        itemIndex = i;
                    }
                    continue;
                }
                Component newLine = getHologramLine(owner, i);
                hologram.setRow(i++, newLine);
            }
        } else {
            hologram.clearRows();
            for (int i = 0; i < config.getStringList("shops.lines").size(); i++) {
                Component line = getHologramLine(owner, i);
                hologram.addRow(line);
            }
        }

        ItemStack displayItem = shop.getItem();
        boolean showBarrier = plugin.getConf().getBoolean("shops.barrier-when-empty");
        if (displayItem == null || (shop.getItemsLeft() == 0 && showBarrier)) {
            displayItem = new ItemStack(Material.BARRIER);
        }
        if (itemIndex != -1 && hologram.getRows().get(itemIndex) instanceof HologramItemRow itemRow) {
            itemRow.setItem(displayItem);
        } else {
            hologram.addRow(displayItem);
        }

    }

    public Component getHologramLine(String owner, int index) {
        String buyPrice = config.getString("shops.price-format.buy")
                .replace("%price%", FormatUtils.formatNumber(shop.getCustomerPurchasePrice()));
        String sellPrice = config.getString("shops.price-format.sell")
                .replace("%price%", FormatUtils.formatNumber(shop.getCustomerSalePrice()));

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
        Component parsedLine = TranslateColor.translate(config.getStringList("shops.lines").get(index)
                .replace("%owner%", owner == null ? config.getString("shops.unknown.owner") : owner)
                .replace("%shop-name%", shop.getName())
                .replace("%price%", priceDisplay)
                .replace("%sell-price%", sellPrice)
                .replace("%buy-price%", buyPrice)
                .replace("%items-left%", String.valueOf(shop.getItemsLeft()))
                .replace("%item%", shop.getItemName()));
        return parsedLine;
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.remove();
        }
    }
}
