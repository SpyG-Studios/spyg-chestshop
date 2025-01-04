package com.spygstudios.chestshop.commands;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.commands.arguments.IntegerArgument;
import com.spygstudios.chestshop.config.Message;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import dev.rollczi.litecommands.message.LiteMessages;
import lombok.Getter;

public class CommandHandler {

    @Getter
    private LiteCommands<CommandSender> commands;

    public CommandHandler(ChestShop plugin) {
        commands = LiteBukkitFactory.builder("spygchestshop", plugin).commands(new Reload(plugin), new ShopList()).message(LiteBukkitMessages.PLAYER_ONLY, Message.PLAYER_ONLY.get())
                .message(LiteMessages.MISSING_PERMISSIONS, Message.NO_PERMISSION.get()).invalidUsage(new InvalUsage()).extension(new LiteAdventureExtension<>())
                .argument(Integer.class, new IntegerArgument()).build();
    }

    public void unregister() {
        if (commands != null) {
            commands.unregister();
        }
    }

}
