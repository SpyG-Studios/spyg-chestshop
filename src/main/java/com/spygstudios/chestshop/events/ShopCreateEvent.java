package com.spygstudios.chestshop.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.spygstudios.chestshop.shop.Shop;

import lombok.Getter;

public class ShopCreateEvent extends Event {

    @Getter
    private final Shop shop;

    private static final HandlerList HANDLERS = new HandlerList();

    public ShopCreateEvent(Shop shop) {
        this.shop = shop;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
