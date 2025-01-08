package com.spygstudios.chestshop.commands.arguments;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.spygstudios.chestshop.config.Message;
import com.spygstudios.chestshop.shop.Shop;
import com.spygstudios.spyglib.components.ComponentUtils;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;

public class ShopArgument extends ArgumentResolver<CommandSender, Shop> {
    @Override
    protected ParseResult<Shop> parse(Invocation<CommandSender> invocation, Argument<Shop> argument, String param) {
        Shop shop = Shop.getShop(param);
        if (shop == null) {
            return ParseResult.failure(ComponentUtils.replaceComponent(Message.SHOP_NOT_FOUND.get(), "%shop%", param));
        } else {
            return ParseResult.success(shop);
        }
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Shop> argument, SuggestionContext context) {
        if (!(invocation.sender() instanceof Player player)) {
            return SuggestionResult.empty();
        }
        List<String> shopNames = Shop.getShops(player).stream().map(Shop::getName).toList();
        List<String> filteredShops = shopNames.stream().filter(name -> name.startsWith(context.getCurrent().lastLevel())).toList();
        return SuggestionResult.of(filteredShops);
    }
}
