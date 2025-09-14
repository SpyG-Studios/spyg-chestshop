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

    public ShopHologram(Shop shop, ChestShop plugin) {
        this.shop = shop;
        this.config = plugin.getConf();
        int hologramRange = config.getInt("shops.holograms.range");
        boolean seeTroughWalls = config.getBoolean("shops.holograms.see-through-walls");
        this.hologram = plugin.getHologramManager().createHologram(shop.getChestLocation().clone().add(0.5, 0.7, 0.5), seeTroughWalls, hologramRange);
        updateHologramRows();
    }

    public void updateHologramRows() {
        while (!hologram.getRows().isEmpty()) {
            hologram.removeRow(0);
        }
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
                    .replace("%material%", shop.getMaterialString())));
        });
        hologram.addRow(new ItemStack(
                shop.getMaterial() == null || (shop.getItemsLeft() == 0 && ChestShop.getInstance().getConf().getBoolean("shops.barrier-when-empty")) ? Material.BARRIER : shop.getMaterial()));
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.remove();
        }
    }
}
