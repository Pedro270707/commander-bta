package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class TellRawCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("tellraw")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("targets", EntityArgumentType.players())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    EntitySelector entitySelector = c.getArgument("targets", EntitySelector.class);
                                    String message = c.getArgument("message", String.class);

                                    List<? extends Entity> players = entitySelector.get(source);

                                    for (Entity player : players) {
                                        source.sendMessage((EntityPlayer) player, message);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
