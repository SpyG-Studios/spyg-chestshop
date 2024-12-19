package com.spygstudios.chestshop.config;

import java.util.Arrays;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.spyglib.yamlmanager.YamlManager;

public class GuiConfig extends YamlManager {

    public GuiConfig(ChestShop plugin) {
        super("guis.yml", plugin);

        set("shop.title", "&8[&6%shop-name%&8] &7» &f%player-name%");
        set("shop.filleritem.name", "-");
        set("shop.filleritem.material", "GRAY_STAINED_GLASS_PANE");
        set("shop.filleritem.lore", Arrays.asList("&r"));

        set("shop.info.title", "&6&lShop Info");
        set("shop.info.lore", Arrays.asList("&6owner: &7%player-name%", "&6material: &7%material%", "&6sell amount: &7%amount%", "&6price: &7$%price%", "&6created: &7%created%",
                "&6location: &7%location%", "&6sold items: &7%sold-items%", "&6money earnd: &7$%money-earnd%"));

        set("shop.notify.title", "&a&lNotifications");
        set("shop.notify.on", "&2ON");
        set("shop.notify.off", "&4OFF");

        saveConfig();
    }

}
