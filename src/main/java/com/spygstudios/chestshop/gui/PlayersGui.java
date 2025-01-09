package com.spygstudios.chestshop.gui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.item.PlayerHeads;

import lombok.Getter;

public class PlayersGui {
    private static GuiConfig config;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new PlayersHolder(player, shop), 27,
                TranslateColor.translate(config.getString("players.title").replace("%shop-name%", shop.getName())));
        for (UUID uuid : shop.getAddedPlayers()) {
            if (Bukkit.getOfflinePlayer(uuid).getName() == null) {
                continue;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            ItemStack skull = offlinePlayer.isOnline() ? PlayerHeads.getOnlinePlayerHead(uuid) : new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.displayName(TranslateColor.translate(config.getString("shop.player.head.title").replace("%player-name%", offlinePlayer.getName())));
            skull.setItemMeta(skullMeta);
            inventory.addItem(skull);

            if (offlinePlayer.isOnline()) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                ItemStack head = PlayerHeads.getOfflinePlayerHead(uuid);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.displayName(TranslateColor.translate(config.getString("shop.player.head.title").replace("%player-name%", offlinePlayer.getName())));
                    head.setItemMeta(meta);
                    inventory.remove(skull);
                    inventory.addItem(head);
                });
            });
        }

        player.openInventory(inventory);
    }

    public static class PlayersHolder implements InventoryHolder {

        @Getter
        private final Player player;

        @Getter
        private final Shop shop;

        public PlayersHolder(Player player, Shop shop) {
            this.player = player;
            this.shop = shop;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
