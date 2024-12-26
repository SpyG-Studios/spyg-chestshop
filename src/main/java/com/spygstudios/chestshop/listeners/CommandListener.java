package com.spygstudios.chestshop.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.color.TranslateColor;

public class CommandListener implements CommandExecutor, Listener {

    public CommandListener(ChestShop plugin, String command) {
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(new TabListener());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Message.PLAYER_ONLY.sendMessage(sender);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(TranslateColor.translate("&cUsage: /%cmd% <list>".replace("%cmd%", label)));
            return true;
        }

        return true;
    }

    public static boolean isPositiveInteger(String str) {
        return str != null && str.matches("[1-9][0-9]*");
    }

}
