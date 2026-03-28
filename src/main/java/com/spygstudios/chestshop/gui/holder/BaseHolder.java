package com.spygstudios.chestshop.gui.holder;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.spygstudios.chestshop.shop.Shop;

import lombok.Getter;

@Getter
public abstract class BaseHolder implements InventoryHolder {
    private final Player player;
    private final Shop shop;

    protected BaseHolder(Player player, Shop shop) {
        this.player = player;
        this.shop = shop;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
