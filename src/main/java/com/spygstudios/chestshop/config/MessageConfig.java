package com.spygstudios.chestshop.config;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

import lombok.Getter;

public class MessageConfig extends YamlManager {

    @Getter
    private String locale;

    public MessageConfig(ChestShop plugin, String locale) {
        super("locale/" + locale + ".yml", plugin);
        this.locale = locale;
    }
}
