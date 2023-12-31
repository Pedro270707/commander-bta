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
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("reset")
                        .then(RequiredArgumentBuilder.argument("position", ChunkCoordinatesArgumentType.chunkCoordinates())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    ChunkCoordinates chunkCoordinates = c.getArgument("position", ChunkCoordinates.class);
                                    World world = source.getWorld();

                                    int x = chunkCoordinates.getX(source);
                                    int z = chunkCoordinates.getZ(source);

                                    world.getChunkProvider().regenerateChunk(x, z);
                                    for (LevelListener listener : world.listeners) {
                                        listener.allChanged();
                                    }
                                    source.sendTranslatableMessage("commands.commander.chunk.reset.success", x, z);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
