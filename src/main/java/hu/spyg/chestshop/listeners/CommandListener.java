package hu.spyg.chestshop.listeners;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import hu.spyg.chestshop.ChestShop;
import hu.spyg.chestshop.config.Config;
import hu.spyg.chestshop.shop.Shop;
import hu.spyg.spyglib.color.TranslateColor;

public class CommandListener implements CommandExecutor, Listener {

    private Config config;

    public CommandListener(ChestShop plugin, String command) {
        plugin.getCommand(command).setExecutor(this);
        plugin.getCommand(command).setTabCompleter(new TabListener());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.config = plugin.getConf();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.getMessage("player-only"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(TranslateColor.translate("&cUsage: /chestshop <create|remove|list>"));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("create")) {
                player.sendMessage(TranslateColor.translate("&cUsage: /chestshop create <name>"));
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                Block targetBlock = player.getTargetBlock((Set<Material>) null, 4);
                if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
                    player.sendMessage(config.getMessage("no-chest"));
                    return true;
                }

                if (!Shop.isChestFaceFree(targetBlock)) {
                    player.sendMessage(config.getMessage("chest-face-not-free"));
                    return true;
                }
                String name = args[1].trim();
                new Shop(player, name, (Chest) targetBlock.getState());

                return true;
            }
        }
        return true;
    }

}
