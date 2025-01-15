package com.spygstudios.chestshop.gui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.item.PlayerHeads;
import com.spygstudios.spyglib.persistentdata.PersistentData;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlayersGui {
    private static GuiConfig config;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        config = plugin.getGuiConfig();
        Inventory inventory = player.getServer().createInventory(new PlayersHolder(player, shop), 27,
                TranslateColor.translate(config.getString("players.title").replace("%shop-name%", shop.getName())));
        setGlassBackground(inventory);
        loadPlayerHeads(plugin, shop, inventory);
        player.openInventory(inventory);
    }

    public static void reloadGui(ChestShop plugin, Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayersHolder holder)) {
            return;
        }
        setGlassBackground(inventory);
        loadPlayerHeads(plugin, holder.getShop(), inventory);
    }

    private static void setGlassBackground(Inventory inventory) {
        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.displayName(TranslateColor.translate("&7"));
            glass.setItemMeta(glassMeta);
            inventory.setItem(i, glass);
        }
    }

    private static void loadPlayerHeads(ChestShop plugin, Shop shop, Inventory inventory) {
        for (int i = 0; i < shop.getAddedPlayers().size(); i++) {
            UUID uuid = shop.getAddedPlayers().get(i);
            if (Bukkit.getOfflinePlayer(uuid).getName() == null) {
                continue;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            ItemStack skull = offlinePlayer.isOnline() ? PlayerHeads.getOnlinePlayerHead(uuid) : new ItemStack(Material.PLAYER_HEAD);
            skull.setItemMeta(getPlayerHeadMeta(skull, offlinePlayer));
            PersistentData skullData = new PersistentData(plugin, skull);
            skullData.set("action", GuiAction.REMOVE_PLAYER.name());
            skullData.set("uuid", offlinePlayer.getUniqueId().toString());
            skullData.save();
            inventory.setItem(i, skull);

            if (offlinePlayer.isOnline()) {
                continue;
            }
            final int index = i;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                ItemStack head = PlayerHeads.getOfflinePlayerHead(uuid);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    inventory.remove(skull);
                    head.setItemMeta(getPlayerHeadMeta(head, offlinePlayer));
                    PersistentData headData = new PersistentData(plugin, head);
                    headData.set("action", GuiAction.REMOVE_PLAYER.name());
                    headData.set("uuid", offlinePlayer.getUniqueId().toString());
                    headData.save();
                    inventory.setItem(index, head);
                });
            });
        }
        if (shop.getAddedPlayers().size() > 18) {
            inventory.setItem(26, new ItemStack(Material.ARROW));
        }
    }

    private static SkullMeta getPlayerHeadMeta(ItemStack skull, OfflinePlayer offlinePlayer) {
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.displayName(TranslateColor.translate(config.getString("players.player.title").replace("%player-name%", offlinePlayer.getName())));
        skullMeta.lore(TranslateColor.translate(config.getStringList("players.player.lore")));
        return skullMeta;
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
