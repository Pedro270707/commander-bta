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

public class SetSpawnCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("setworldspawn")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            IntegerCoordinates coordinates = c.getArgument("position", IntegerCoordinates.class);

                            int x = coordinates.getX(source);
                            int y = coordinates.getY(source, true);
                            int z = coordinates.getZ(source);

                            source.getWorld().setSpawnPoint(new ChunkCoordinates(x, y, z));
                            source.sendTranslatableMessage("commands.commander.setspawn.success", x, y, z);

                            return Command.SINGLE_SUCCESS;
                        })));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("setspawn")
                .requires(CommanderCommandSource::hasAdmin)
                .redirect(command));
    }
}
