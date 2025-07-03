package com.spygstudios.chestshop.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.color.TranslateColor;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.Getter;

@Command(name = "spygchestshop admin customer", aliases = { "spcs admin customer", "chestshop admin customer", "scs admin customer" })
public class CustomerMode {

    @Getter
    private static List<UUID> customerMode = new ArrayList<>();

    @Execute
    @Permission("spygchestshop.*")
    @Permission("spygchestshop.admin.*")
    @Permission("spygchestshop.admin.customermode")
    public void onCustomerMode(@Context Player player, @Arg Optional<Player> target) {
        Player targetPlayer = target.orElse(player);
        String state = !customerMode.contains(targetPlayer.getUniqueId()) ? Message.ADMIN_CUSTOMER_MODE_STATE_ENABLED.getRaw() : Message.ADMIN_CUSTOMER_MODE_STATE_DISABLED.getRaw();
        String message = Message.ADMIN_CUSTOMER_MODE.getRaw().replace("%state%", state).replace("%prefix%", Message.getPrefix());

        if (customerMode.contains(targetPlayer.getUniqueId())) {
            customerMode.remove(targetPlayer.getUniqueId());
            player.sendMessage(TranslateColor.translate(message));
            if (!player.equals(targetPlayer)) {
                String otherMessage = Message.ADMIN_CUSTOMER_MODE.getRaw().replace("%state%", Message.ADMIN_CUSTOMER_MODE_STATE_DISABLED.getRaw()).replace("%player-name%", player.getName());
                player.sendMessage(TranslateColor.translate(otherMessage.replace("%prefix%", Message.getPrefix())));
            }
        } else {
            customerMode.add(player.getUniqueId());
            player.sendMessage(TranslateColor.translate(message));
            if (!player.equals(targetPlayer)) {
                String otherMessage = Message.ADMIN_CUSTOMER_MODE.getRaw().replace("%state%", Message.ADMIN_CUSTOMER_MODE_STATE_DISABLED.getRaw()).replace("%player-name%", player.getName());
                player.sendMessage(TranslateColor.translate(otherMessage.replace("%prefix%", Message.getPrefix())));
            }
        }
    }
}
