package com.spygstudios.chestshop.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.hologram.Hologram;

import lombok.Getter;

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
        hologram.getRows().clear();
        hologram.setViewDistance(config.getInt("shops.holograms.range"));
        hologram.setSeeTrough(config.getBoolean("shops.holograms.see-through-walls"));
        String owner = Bukkit.getOfflinePlayer(shop.getOwnerId()).getName();
        config.getStringList("shops.lines").forEach(line -> {

            String buyPrice = config.getString("shops.price-format.buy")
                    .replace("%price%", String.valueOf(shop.getCustomerPurchasePrice()));
            String sellPrice = config.getString("shops.price-format.sell")
                    .replace("%price%", String.valueOf(shop.getCustomerSalePrice()));

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

            hologram.addRow(TranslateColor.translate(line
                    .replace("%owner%", owner == null ? config.getString("shops.unknown.owner") : owner)
                    .replace("%shop-name%", shop.getName())
                    .replace("%price%", priceDisplay)
                    .replace("%sell-price%", sellPrice)
                    .replace("%buy-price%", buyPrice)
                    .replace("%items-left%", String.valueOf(shop.getItemsLeft()))
                    .replace("%item%", shop.getItemName())));
        });
        ItemStack displayItem = shop.getItem();
        boolean showBarrier = plugin.getConf().getBoolean("shops.barrier-when-empty");
        if (displayItem == null || (shop.getItemsLeft() == 0 && showBarrier)) {
            displayItem = new ItemStack(Material.BARRIER);
        }
        hologram.addRow(displayItem);
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.remove();
        }
    }
}
