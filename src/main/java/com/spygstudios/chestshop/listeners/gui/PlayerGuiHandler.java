package com.spygstudios.chestshop.listeners.gui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.enums.GuiAction;
import com.spygstudios.chestshop.gui.PlayersGui;
import com.spygstudios.chestshop.gui.PlayersGui.PlayersHolder;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.persistentdata.PersistentData;

public class PlayerGuiHandler implements Listener {

    private final ChestShop plugin;

    public PlayerGuiHandler(ChestShop plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayersGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlayersHolder)) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        event.setCancelled(true);
        PersistentData data = new PersistentData(plugin, clickedItem);
        String action = data.getString("action");
        if (action == null) {
            return;
        }
        PlayersHolder holder = (PlayersHolder) event.getInventory().getHolder();
        GuiAction guiAction = GuiAction.valueOf(action);
        switch (guiAction) {
            case REMOVE_PLAYER:
                Shop shop = holder.getShop();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(data.getString("uuid")));
                shop.removePlayer(offlinePlayer.getUniqueId());
                PlayersGui.reloadGui(plugin, event.getInventory());
                break;

            case NEXT:
                holder.setPage(holder.getPage() + 1);
                PlayersGui.reloadGui(plugin, event.getInventory());
                break;
            case BACK:
                holder.setPage(holder.getPage() - 1);
                PlayersGui.reloadGui(plugin, event.getInventory());
                break;
            default:
                break;
        }
    }

}
