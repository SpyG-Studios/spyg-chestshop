package com.spygstudios.chestshop.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.spygstudios.chestshop.ChestShop;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FormatUtils {

    public static double parsePrice(double price) {
        ChestShop plugin = ChestShop.getInstance();
        boolean decimalsEnabled = plugin.getConf().getBoolean("shops.decimals.enabled");
        int maxDecimals = decimalsEnabled ? plugin.getConf().getInt("shops.decimals.max") : 0;
        return new BigDecimal(price)
                .setScale(maxDecimals, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static String formatNumber(double number) {
        DecimalFormat df = new DecimalFormat("#,##0.##########");
        return df.format(number);
    }

}
