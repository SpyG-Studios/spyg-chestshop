package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.enums.GuiAction;

import lombok.Getter;

public class AmountHandler {
    @Getter
    private static List<AmountHandler> pendingAmount = new ArrayList<>();

    @Getter
    private Player player;

    @Getter
    private double amount;

    @Getter
    private GuiAction type;

    @Getter
    private Shop shop;

    public AmountHandler(Player player, Shop shop, GuiAction type) {
        this.player = player;
        this.type = type;
        this.shop = shop;
        Message.ENTER_AMOUNT.send(player, Map.of("%cancel%", ChestShop.getInstance().getConf().getString("cancel")));
        pendingAmount.add(this);
    }

    public void create(double amount) {
        if (type.equals(GuiAction.SET_ITEM_PRICE)) {
            shop.setPrice(amount);
        }
        Message.ENTER_AMOUNT_SUCCESS.send(player);
        this.amount = amount;
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
