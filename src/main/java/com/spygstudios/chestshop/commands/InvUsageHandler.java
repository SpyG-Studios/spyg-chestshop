package com.spygstudios.chestshop.commands;

import org.bukkit.command.CommandSender;

import com.spygstudios.spyglib.color.TranslateColor;

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
            sender.sendMessage(TranslateColor.translate("&cHelytelen használat!"));
            return;
        }

        sender.sendMessage("Összes parancs:");
        for (String scheme : schematic.all()) {
            sender.sendMessage(TranslateColor.translate("&8- " + getCommand(scheme, invocation.label())));
        }

    }

    private String getCommand(String scheme, String label) {
        String[] args = scheme.split(" ");
        args[0] = "&7/" + label + "&f";
        return String.join(" ", args);
    }

}
