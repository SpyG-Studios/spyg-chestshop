package com.spygstudios.chestshop.gui;

import java.util.List;
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
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlayersGui {
    private static GuiConfig config;

    public static void open(ChestShop plugin, Player player, Shop shop) {
        config = plugin.getGuiConfig();
        PlayersHolder holder = new PlayersHolder(player, shop);
        Inventory inventory = player.getServer().createInventory(holder, 27, TranslateColor.translate(config.getString("players.title").replace("%shop-name%", shop.getName())));
        setGlassBackground(inventory);
        loadPlayerHeads(plugin, shop, inventory, holder.getPage());
        player.openInventory(inventory);
    }

    public static void reloadGui(ChestShop plugin, Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayersHolder holder)) {
            return;
        }
        setGlassBackground(inventory);
        loadPlayerHeads(plugin, holder.getShop(), inventory, holder.getPage());
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

    private static void loadPlayerHeads(ChestShop plugin, Shop shop, Inventory inventory, int page) {
        int headPerPage = 18;
        int maxPage = (int) Math.ceil((double) shop.getAddedPlayers().size() / headPerPage);
        List<UUID> addedPlayers = shop.getAddedPlayers().stream().skip((long) (page - 1) * headPerPage).limit(headPerPage).toList();

        for (int i = 0; i < addedPlayers.size(); i++) {
            UUID uuid = addedPlayers.get(i);
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
        if (page > 1) {
            inventory.setItem(18, getArrowItem(plugin, config.getString("players.back.title"), GuiAction.BACK));
        }
        if (shop.getAddedPlayers().size() > 18 && page < maxPage) {
            inventory.setItem(26, getArrowItem(plugin, config.getString("players.next.title"), GuiAction.NEXT));
        }
    }

    private static ItemStack getArrowItem(ChestShop plugin, String displayName, GuiAction action) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.displayName(TranslateColor.translate(displayName));
        arrow.setItemMeta(arrowMeta);
        PersistentData nextData = new PersistentData(plugin, arrow);
        nextData.set("action", action.name());
        nextData.save();
        return arrow;
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

        @Getter
        @Setter
        private int page;

        public PlayersHolder(Player player, Shop shop) {
            this.player = player;
            this.shop = shop;
            this.page = 1;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

    }
}
