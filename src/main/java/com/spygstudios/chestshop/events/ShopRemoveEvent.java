package com.spygstudios.chestshop.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.spygstudios.chestshop.enums.ShopRemoveCause;
import com.spygstudios.chestshop.shop.Shop;

import lombok.Getter;
import lombok.Setter;

public class ShopRemoveEvent extends Event {

    @Getter
    private final Shop shop;

    @Getter
    private final ShopRemoveCause cause;

    @Getter
    private final Player shopRemover;

    @Getter
    @Setter
    private boolean cancelled = false;

    private static final HandlerList HANDLERS = new HandlerList();

    public ShopRemoveEvent(Shop shop, ShopRemoveCause cause, Player shopRemover) {
        this.shop = shop;
        this.cause = cause;
        this.shopRemover = shopRemover;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
