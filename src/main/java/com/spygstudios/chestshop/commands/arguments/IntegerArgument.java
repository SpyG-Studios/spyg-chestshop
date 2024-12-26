package com.spygstudios.chestshop.commands.arguments;

import org.bukkit.command.CommandSender;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;

public class IntegerArgument extends ArgumentResolver<CommandSender, Integer> {
    @Override
    protected ParseResult<Integer> parse(Invocation<CommandSender> invocation, Argument<Integer> argument, String param) {
        try {
            return ParseResult.success(Integer.parseInt(param));
        } catch (NumberFormatException e) {
            return ParseResult.failure();
        }
    }
}
