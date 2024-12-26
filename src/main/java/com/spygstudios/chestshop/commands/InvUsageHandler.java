package com.spygstudios.chestshop.commands;

import org.bukkit.command.CommandSender;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;

public class InvUsageHandler implements InvalidUsageHandler<CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        Schematic schematic = result.getSchematic();
        CommandSender sender = invocation.sender();
        if (schematic.isOnlyFirst()) {
            sender.sendMessage("Ez így nem fain");
            return;
        }

        sender.sendMessage("Összes parancs:");
        for (String scheme : schematic.all()) {
            sender.sendMessage("§8 - §7" + scheme);
        }

    }

}
