package com.spygstudios.chestshop.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.hologram.Hologram;

import lombok.Getter;

public class ShopHologram {

    @Getter
    private final Hologram hologram;
    private final Shop shop;
    private final ChestShop plugin;

    public ShopHologram(Shop shop, ChestShop plugin) {
        this.plugin = plugin;
        this.shop = shop;
        this.hologram = plugin.getHologramManager().createHologram(shop.getChestLocation().clone().add(0.5, 0.7, 0.5));
        updateHologramRows();
    }

    public void updateHologramRows() {
        while (!hologram.getRows().isEmpty()) {
            hologram.removeRow(0);
        }
        String owner = Bukkit.getOfflinePlayer(shop.getOwnerId()).getName();
        plugin.getConf().getStringList("shops.lines").forEach(line -> hologram.addRow(TranslateColor.translate(line.replace("%owner%", owner == null ? "Unknown" : owner)
                .replace("%shop-name%", shop.getName()).replace("%price%", String.valueOf(shop.getPrice())).replace("%material%", shop.getMaterialString()))));
        hologram.addRow(new ItemStack(
                shop.getMaterial() == null || (shop.getItemsLeft() == 0 && ChestShop.getInstance().getConf().getBoolean("shops.barrier-when-empty")) ? Material.BARRIER : shop.getMaterial()));
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.remove();
        }
    }
}
