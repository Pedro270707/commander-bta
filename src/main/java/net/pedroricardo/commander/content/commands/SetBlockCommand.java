package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.block.Block;
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
                .then(RequiredArgumentBuilder.argument("pos", BlockCoordinatesArgumentType.blockCoordinates())
                        .then(RequiredArgumentBuilder.argument("block", BlockArgumentType.block())
                                .executes(c -> {
                                    BlockCoordinates coordinates = c.getArgument("pos", BlockCoordinates.class);
                                    Block block = c.getArgument("block", Block.class);

                                    ((CommanderCommandSource)c.getSource()).getWorld().setBlockWithNotify(coordinates.getX((CommanderCommandSource)c.getSource()), coordinates.getY((CommanderCommandSource)c.getSource(), true), coordinates.getZ((CommanderCommandSource)c.getSource()), block.id);

                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.argument("metadata", IntegerArgumentType.integer(0, 255))
                                        .executes(c -> {
                                            BlockCoordinates coordinates = c.getArgument("pos", BlockCoordinates.class);
                                            Block block = c.getArgument("block", Block.class);
                                            Vec3d sourceCoordinates = ((CommanderCommandSource)c.getSource()).getCoordinates();
                                            int metadata = c.getArgument("metadata", Integer.class);

                                            ((CommanderCommandSource)c.getSource()).getWorld().setBlockAndMetadataWithNotify(coordinates.getX((CommanderCommandSource)c.getSource()), coordinates.getY((CommanderCommandSource)c.getSource(), true), coordinates.getZ((CommanderCommandSource)c.getSource()), block.id, metadata);

                                            return CommanderCommandManager.SINGLE_SUCCESS;
                                        })))));
    }
}
