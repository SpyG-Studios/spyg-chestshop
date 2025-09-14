package com.spygstudios.chestshop.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.config.MessageConfig;
import com.spygstudios.chestshop.gui.DashboardGui.DashboardHolder;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.gui.ShopGui.ShopHolder;
import com.spygstudios.chestshop.shop.Shop;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

@Command(name = "spygchestshop reload", aliases = { "spcs reload", "chestshop reload", "scs reload" })
public class Reload {
    Config config;
    GuiConfig guiConfig;
    ChestShop plugin;
    MessageConfig messageConfig;

    public Reload(ChestShop plugin) {
        this.config = plugin.getConf();
        this.guiConfig = plugin.getGuiConfig();
        this.messageConfig = plugin.getMessageConfig();
        this.plugin = plugin;
    }

    @Execute
    @Permission("spygchestshop.admin.reload")
    public void onReload(@Context CommandSender sender) {
        config.reloadConfig();
        guiConfig.reloadConfig();
        plugin.getMessageConfig().reloadConfig();
        for (Shop shop : Shop.getShops()) {
            shop.getHologram().updateHologramRows();
        }
        if (!config.getString("locale").equals(plugin.getMessageConfig().getLocale())) {
            plugin.setMessageConfig(new MessageConfig(plugin, config.getString("locale")));
            Message.init(plugin.getMessageConfig());
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) {
                continue;
            }
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof DashboardHolder || player.getOpenInventory().getTopInventory().getHolder() instanceof ShopHolder
                    || player.getOpenInventory().getTopInventory().getHolder() instanceof PlayersHolder) {
                player.closeInventory();
            }
        }
        Message.CONFIG_RELOADED.send(sender);
    }
}
