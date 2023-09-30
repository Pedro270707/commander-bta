package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.block.Block;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.collection.Pair;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.*;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

@SuppressWarnings("unchecked")
public class SetBlockCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.setblock.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("setblock")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                        .then(RequiredArgumentBuilder.argument("block", BlockArgumentType.block())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    IntegerCoordinates coordinates = c.getArgument("position", IntegerCoordinates.class);
                                    Pair<Block, Integer> pair = c.getArgument("block", Pair.class);

                                    if (!source.getWorld().isBlockLoaded(coordinates.getX(source), coordinates.getY(source, true), coordinates.getZ(source))) {
                                        throw FAILURE.create();
                                    } else {
                                        source.getWorld().setBlockAndMetadataWithNotify(coordinates.getX(source), coordinates.getY(source, true), coordinates.getZ(source), pair.getLeft().id, pair.getRight());
                                        source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.setblock.success", coordinates.getX(source), coordinates.getY(source, true), coordinates.getZ(source)));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
