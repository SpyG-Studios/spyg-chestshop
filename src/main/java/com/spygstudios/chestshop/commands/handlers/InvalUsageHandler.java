package com.spygstudios.chestshop.commands.handlers;

import org.bukkit.command.CommandSender;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Config;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.color.TranslateColor;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invalidusage.InvalidUsageHandler;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;

public class InvalUsageHandler implements InvalidUsageHandler<CommandSender> {

    Config config;

    public InvalUsageHandler(ChestShop plugin) {
        config = plugin.getConf();
    }

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        Schematic schematic = result.getSchematic();
        CommandSender sender = invocation.sender();
        if (schematic.isOnlyFirst()) {
            sender.sendMessage(TranslateColor.translate(Message.USAGE.getRaw().replace("%prefix%", Message.getPrefix()).replace("%usage%", getCommand(schematic.first(), invocation.label()))));
            return;
        }

        Message.COMMANDS.send(sender);
        for (String scheme : schematic.all()) {
            sender.sendMessage(TranslateColor.translate(config.getString("colors.command.list") + getCommand(scheme, invocation.label())));
        }

    }

    private String getCommand(String scheme, String label) {
        String[] args = scheme.split(" ");
        args[0] = config.getString("colors.command.label") + "/" + label + " "; // Az első argumentum kicserélése

        StringBuilder result = new StringBuilder(args[0]);

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("<") && arg.endsWith(">")) {
                result.append(config.getString("colors.command.required-arg")).append(arg);
            } else if (arg.startsWith("[") && arg.endsWith("]")) {
                result.append(config.getString("colors.command.optional-arg")).append(arg);
            } else {
                result.append(config.getString("colors.command.args")).append(arg);
            }
        }

        return result.toString();
    }

}
