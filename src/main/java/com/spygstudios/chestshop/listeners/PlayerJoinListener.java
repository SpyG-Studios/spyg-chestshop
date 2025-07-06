package com.spygstudios.chestshop.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.components.ComponentUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;

public class PlayerJoinListener implements Listener {

    private final ChestShop plugin;
    private final String currentVersion;
    private final boolean isLatestVersion;

    public PlayerJoinListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.currentVersion = plugin.getCurrentVersion();
        this.isLatestVersion = plugin.isLatestVersion();
        this.plugin = plugin;
    }

    @EventHandler
    public void chekUpdatesOnJoin(PlayerJoinEvent event) {
        if (!plugin.getConf().getBoolean("check-for-updates")) {
            return;
        }
        if (!isLatestVersion && event.getPlayer().hasPermission("spygchestshop.admin.updates")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Component message = ComponentUtils.replaceComponent(Message.NEW_VERSION.get().clickEvent(ClickEvent.clickEvent(Action.OPEN_URL, "https://modrinth.com/plugin/spyg-chestshop")), Map.of(
                        "%old-version%", plugin.getPluginMeta().getVersion(),
                        "%new-version%", currentVersion));
                event.getPlayer().sendMessage(message);
            }, 60L);
        }
    }

    @EventHandler
    public void loadShopsOnJoin(PlayerJoinEvent event) {
        plugin.getDataManager().loadPlayerShops(event.getPlayer().getUniqueId());
    }

}
