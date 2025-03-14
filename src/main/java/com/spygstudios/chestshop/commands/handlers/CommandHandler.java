package com.spygstudios.chestshop.commands.handlers;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.commands.AddPlayer;
import com.spygstudios.chestshop.commands.Create;
import com.spygstudios.chestshop.commands.RemovePlayer;
import com.spygstudios.chestshop.commands.Rename;
import com.spygstudios.chestshop.commands.ShopList;
import com.spygstudios.chestshop.commands.admin.CustomerMode;
import com.spygstudios.chestshop.commands.admin.Reload;
import com.spygstudios.chestshop.commands.admin.ShopListAdmin;
import com.spygstudios.chestshop.commands.arguments.IntegerArgument;
import com.spygstudios.chestshop.commands.arguments.ShopArgument;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;

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
        commands = LiteBukkitFactory.builder("spygchestshop", plugin)

                .commands(new Reload(plugin), new ShopList(), new ShopListAdmin(), new AddPlayer(plugin), new RemovePlayer(plugin), new Create(), new Rename(plugin), new CustomerMode())

                .message(LiteBukkitMessages.PLAYER_ONLY, Message.PLAYER_ONLY.get())

                .message(LiteMessages.MISSING_PERMISSIONS, Message.NO_PERMISSION.get())
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, Message.PLAYER_NOT_FOUND.get())

                .invalidUsage(new InvalUsageHandler(plugin)).extension(new LiteAdventureExtension<>())

                .argument(Shop.class, new ShopArgument())

                .argument(Integer.class, new IntegerArgument())

                .build();
    }

    public void unregister() {
        if (commands != null) {
            commands.unregister();
        }
    }

}
