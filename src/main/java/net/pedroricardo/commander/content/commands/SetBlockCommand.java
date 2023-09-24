package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.*;
import net.pedroricardo.commander.content.helpers.BlockCoordinates;

@SuppressWarnings("unchecked")
public class SetBlockCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("setblock")
                .requires(sourceStack -> ((CommanderCommandSource)sourceStack).hasAdmin())
                .then(RequiredArgumentBuilder.argument("position", BlockCoordinatesArgumentType.blockCoordinates())
                        .then(RequiredArgumentBuilder.argument("block", BlockArgumentType.block())
                                .executes(c -> {
                                    BlockCoordinates coordinates = c.getArgument("position", BlockCoordinates.class);
                                    Pair<Block, Integer> pair = c.getArgument("block", Pair.class);

                                    ((CommanderCommandSource)c.getSource()).getWorld().setBlockAndMetadataWithNotify(coordinates.getX((CommanderCommandSource)c.getSource()), coordinates.getY((CommanderCommandSource)c.getSource()), coordinates.getZ((CommanderCommandSource)c.getSource()), pair.getLeft().id, pair.getRight());

                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                }))));
    }
}
