package com.spygstudios.chestshop.interfaces;

import org.bukkit.entity.Player;

public interface ShopTransactions {
    void sell(Player buyer, int amount);

    void buy(Player seller, int amount);
}
