package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

@SuppressWarnings("unchecked")
public class SetSpawnCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<Object> command = dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("setworldspawn")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then((RequiredArgumentBuilder) RequiredArgumentBuilder.argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                        .executes(c -> {
                            IntegerCoordinates coordinates = c.getArgument("position", IntegerCoordinates.class);

                            int x = coordinates.getX((CommanderCommandSource) c.getSource());
                            int y = coordinates.getY((CommanderCommandSource) c.getSource(), true);
                            int z = coordinates.getZ((CommanderCommandSource) c.getSource());

                            ((CommanderCommandSource)c.getSource()).getWorld().setSpawnPoint(new ChunkCoordinates(x, y, z));

                            return Command.SINGLE_SUCCESS;
                        })));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("setspawn")
                .redirect(command));
    }
}
