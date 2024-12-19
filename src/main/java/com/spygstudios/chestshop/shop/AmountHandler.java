package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import lombok.Getter;

public class AmountHandler {
    @Getter
    private static List<AmountHandler> pendingAmount = new ArrayList<>();

    @Getter
    private Player player;

    @Getter
    private double amount;

    public AmountHandler(Player player) {
        if (pendingAmount.contains(this)) {
            return;
        }

        this.player = player;

        pendingAmount.add(this);
    }

    public void create(int amount) {

        cancel();
    }

    public void cancel() {
        pendingAmount.remove(this);
    }

    public static AmountHandler getPendingAmount(Player player) {
        for (AmountHandler pendingNeed : pendingAmount) {
            if (pendingNeed.getPlayer().equals(player)) {
                return pendingNeed;
            }
        }
        return null;
    }
}
