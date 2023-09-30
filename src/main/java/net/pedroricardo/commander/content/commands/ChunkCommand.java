package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.ChunkCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.ChunkCoordinates;

@SuppressWarnings("unchecked")
public class ChunkCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("chunk")
                .then((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("reset")
                        .then(RequiredArgumentBuilder.argument("position", ChunkCoordinatesArgumentType.chunkCoordinates())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    ChunkCoordinates chunkCoordinates = c.getArgument("position", ChunkCoordinates.class);
                                    World world = source.getWorld();

                                    world.getChunkProvider().regenerateChunk(chunkCoordinates.getX(source), chunkCoordinates.getZ(source));
                                    for (LevelListener listener : world.listeners) {
                                        listener.allChanged();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
