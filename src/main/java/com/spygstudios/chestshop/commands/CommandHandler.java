package com.spygstudios.chestshop.commands;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.commands.arguments.IntegerArgument;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;

public class CommandHandler {

    @Getter
    private LiteCommands<CommandSender> commands;

    public CommandHandler(ChestShop plugin) {
        commands = LiteBukkitFactory.builder("spygchestshop", plugin).commands(new Reload(plugin), new ShopList()).invalidUsage(new InvUsageHandler()).argument(Integer.class, new IntegerArgument())
                .build();
    }

    public void unregister() {
        if (commands != null) {
            commands.unregister();
        }
    }

}
