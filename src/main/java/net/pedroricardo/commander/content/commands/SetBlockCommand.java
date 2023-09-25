package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.collection.Pair;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.*;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

@SuppressWarnings("unchecked")
public class SetBlockCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("setblock")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                        .then(RequiredArgumentBuilder.argument("block", BlockArgumentType.block())
                                .executes(c -> {
                                    IntegerCoordinates coordinates = c.getArgument("position", IntegerCoordinates.class);
                                    Pair<Block, Integer> pair = c.getArgument("block", Pair.class);

                                    if (!((CommanderCommandSource)c.getSource()).getWorld().isBlockLoaded(coordinates.getX((CommanderCommandSource)c.getSource()), coordinates.getY((CommanderCommandSource)c.getSource(), true), coordinates.getZ((CommanderCommandSource)c.getSource()))) {
                                        ((CommanderCommandSource)c.getSource()).sendMessage("§e" + I18n.getInstance().translateKey("commands.commander.setblock.failure"));
                                    } else {
                                        ((CommanderCommandSource)c.getSource()).getWorld().setBlockAndMetadataWithNotify(coordinates.getX((CommanderCommandSource)c.getSource()), coordinates.getY((CommanderCommandSource)c.getSource(), true), coordinates.getZ((CommanderCommandSource)c.getSource()), pair.getLeft().id, pair.getRight());
                                        ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.setblock.success", coordinates.getX((CommanderCommandSource)c.getSource()), coordinates.getY((CommanderCommandSource)c.getSource(), true), coordinates.getZ((CommanderCommandSource)c.getSource())));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
