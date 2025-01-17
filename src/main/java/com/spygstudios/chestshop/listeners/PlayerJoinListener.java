package com.spygstudios.chestshop.listeners;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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

    private static final String API_URL = "https://hangar.papermc.io/api/v1/projects/Spyg-ChestShop/latestrelease";

    private final ChestShop plugin;

    public PlayerJoinListener(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConf().getBoolean("check-for-updates")) {
            return;
        }
        if (event.getPlayer().hasPermission("spygchestshop.admin.updates")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int versionDifference = checkVersionDifference();
                    if (versionDifference > 0) {
                        String latestVersion = fetchLatestVersion();
                        Component message = ComponentUtils.replaceComponent(
                                Message.NEW_VERSION.get().clickEvent(ClickEvent.clickEvent(Action.OPEN_URL, "https://hangar.papermc.io/SpygStudios/Spyg-ChestShop")),
                                Map.of("%old-version%", plugin.getPluginMeta().getVersion(), "%new-version%", latestVersion));
                        event.getPlayer().sendMessage(message);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Nem sikerült lekérni a verzióadatokat: " + e.getMessage());
                }
            });
        }
    }

    private int checkVersionDifference() throws Exception {
        String latestVersion = fetchLatestVersion();
        if (latestVersion == null) {
            return -1;
        }
        String currentVersion = plugin.getPluginMeta().getVersion();
        return calculateVersionDifference(currentVersion, latestVersion);
    }

    private String fetchLatestVersion() throws Exception {
        URL url = URI.create(API_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("accept", "text/plain");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return reader.readLine().trim(); // A verziószámot adja vissza, pl. "1.0.0"
            }
        } else {
            String warnin = "An error happend during the version check http code: " + responseCode;
            plugin.getLogger().warning(warnin);
        }
        return null;
    }

    private int calculateVersionDifference(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        int maxLength = Math.max(currentParts.length, latestParts.length);
        for (int i = 0; i < maxLength; i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

            if (currentPart < latestPart) {
                return 1;
            } else if (currentPart > latestPart) {
                return 0;
            }
        }
        return 0;
    }
}
