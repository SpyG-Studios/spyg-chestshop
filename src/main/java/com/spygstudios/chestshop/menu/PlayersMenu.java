package com.spygstudios.chestshop.menu;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.GuiConfig;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.menu.holder.BaseHolder;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.chestshop.utils.PageUtil;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.datacontainer.ItemContainer;
import com.spygstudios.spyglib.item.PlayerHeads;

import lombok.Getter;
import lombok.Setter;

public class PlayersMenu implements Listener {

    private final ChestShop plugin;

    public PlayersMenu(ChestShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, Shop shop) {
        GuiConfig config = plugin.getGuiConfig();
        PlayersHolder holder = new PlayersHolder(player, shop);
        Inventory inventory = player.getServer().createInventory(
                holder, 27,
                TranslateColor.translate(config.getString("players.title").replace("%shop-name%", shop.getName())));

        PageUtil.setFillItems(inventory, "players");
        loadPlayerHeads(config, shop, inventory, holder.getPage());
        player.openInventory(inventory);
    }

    private void reloadGui(Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayersHolder holder)) {
            return;
        }
        GuiConfig config = plugin.getGuiConfig();
        PageUtil.setFillItems(inventory, "players");
        loadPlayerHeads(config, holder.getShop(), inventory, holder.getPage());
    }

    private void loadPlayerHeads(GuiConfig config, Shop shop, Inventory inventory, int page) {
        int headPerPage = 18;
        int maxPage = (int) Math.ceil((double) shop.getAddedPlayers().size() / headPerPage);
        List<UUID> addedPlayers = shop.getAddedPlayers().stream()
                .skip((long) (page - 1) * headPerPage)
                .limit(headPerPage)
                .toList();

        for (int i = 0; i < addedPlayers.size(); i++) {
            UUID uuid = addedPlayers.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getName() == null) {
                continue;
            }

            ItemStack skull = offlinePlayer.isOnline()
                    ? PlayerHeads.getOnlinePlayerHead(uuid)
                    : new ItemStack(Material.PLAYER_HEAD);
            skull.setItemMeta(createPlayerHeadMeta(config, skull, offlinePlayer));

            ItemContainer skullData = ItemContainer.create(plugin, skull);
            skullData.set("action", GuiAction.REMOVE_PLAYER.name());
            skullData.set("uuid", offlinePlayer.getUniqueId().toString());
            inventory.setItem(i, skull);

            if (!offlinePlayer.isOnline()) {
                fetchOfflineHead(config, offlinePlayer, inventory, skull, i);
            }
        }

        if (page > 1) {
            inventory.setItem(18, createArrowItem(config, "players.back", GuiAction.BACK));
        }
        if (shop.getAddedPlayers().size() > headPerPage && page < maxPage) {
            inventory.setItem(26, createArrowItem(config, "players.next", GuiAction.NEXT));
        }
    }

    private void fetchOfflineHead(GuiConfig config, OfflinePlayer offlinePlayer, Inventory inventory, ItemStack placeholder, int index) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ItemStack head = PlayerHeads.getOfflinePlayerHead(offlinePlayer.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory.remove(placeholder);
                head.setItemMeta(createPlayerHeadMeta(config, head, offlinePlayer));
                ItemContainer headData = ItemContainer.create(plugin, head);
                headData.set("action", GuiAction.REMOVE_PLAYER.name());
                headData.set("uuid", offlinePlayer.getUniqueId().toString());
                inventory.setItem(index, head);
            });
        });
    }

    private ItemStack createArrowItem(GuiConfig config, String configPath, GuiAction action) {
        Material material = Material.getMaterial(config.getString(configPath + ".material", "ARROW"));
        ItemStack arrow = new ItemStack(material);
        ItemMeta meta = arrow.getItemMeta();
        meta.displayName(TranslateColor.translate(config.getString(configPath + ".title")));
        arrow.setItemMeta(meta);
        ItemContainer.create(plugin, arrow).set("action", action.name());
        return arrow;
    }

    private SkullMeta createPlayerHeadMeta(GuiConfig config, ItemStack skull, OfflinePlayer offlinePlayer) {
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.displayName(TranslateColor.translate(
                config.getString("players.player.title").replace("%player-name%", offlinePlayer.getName())));
        meta.lore(TranslateColor.translate(config.getStringList("players.player.lore")));
        return meta;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlayersHolder holder)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        ItemContainer data = ItemContainer.create(plugin, clickedItem);
        String actionStr = data.getString("action");
        if (actionStr == null) {
            return;
        }

        GuiAction action = GuiAction.valueOf(actionStr);
        switch (action) {
            case REMOVE_PLAYER -> {
                UUID uuid = UUID.fromString(data.getString("uuid"));
                holder.getShop().removePlayer(uuid);
                reloadGui(event.getInventory());
            }
            case NEXT -> {
                holder.setPage(holder.getPage() + 1);
                reloadGui(event.getInventory());
            }
            case BACK -> {
                holder.setPage(holder.getPage() - 1);
                reloadGui(event.getInventory());
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlayersHolder holder)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> plugin.getDashboardGui().open((Player) event.getPlayer(), holder.getShop()), 1);
    }

    public static class PlayersHolder extends BaseHolder {
        @Getter
        @Setter
        private int page;

        public PlayersHolder(Player player, Shop shop) {
            super(player, shop);
            this.page = 1;
        }
    }
}
