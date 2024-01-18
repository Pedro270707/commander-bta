package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.ChunkCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.Coordinates2D;

public class ChunkCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("chunk")
                .requires(CommanderCommandSource::hasAdmin)
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("reset")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Coordinates2D>argument("position", ChunkCoordinatesArgumentType.chunkCoordinates())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    Coordinates2D coordinates2D = c.getArgument("position", Coordinates2D.class);
                                    World world = source.getWorld();

                                    int x = coordinates2D.getX(source);
                                    int z = coordinates2D.getZ(source);

                                    world.getChunkProvider().regenerateChunk(x, z);
                                    for (LevelListener listener : world.listeners) {
                                        listener.allChanged();
                                    }
                                    source.sendTranslatableMessage("commands.commander.chunk.reset.success", x, z);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}