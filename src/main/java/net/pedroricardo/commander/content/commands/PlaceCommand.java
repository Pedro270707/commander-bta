package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.arguments.WorldFeatureArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

public class PlaceCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.place.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("place")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, WorldFeature>argument("feature", WorldFeatureArgumentType.worldFeature())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    WorldFeature feature = c.getArgument("feature", WorldFeature.class);
                                    IntegerCoordinates coordinates = c.getArgument("position", IntegerCoordinates.class);
                                    World world = source.getWorld();

                                    if (coordinates == null) throw CommanderExceptions.notInWorld().create();

                                    int x = coordinates.getX(source);
                                    int y = coordinates.getY(source, true);
                                    int z = coordinates.getZ(source);

                                    boolean success = feature.generate(world, source.getWorld().rand, x, y, z);
                                    if (success) {
                                        for (LevelListener listener : world.listeners) {
                                            listener.allChanged();
                                        }
                                        source.sendTranslatableMessage("commands.commander.place.success", feature.getClass().getSimpleName().substring(12), x, y, z);
                                    } else {
                                        throw FAILURE.create();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("generate")
                .requires(CommanderCommandSource::hasAdmin)
                .redirect(command));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("gen")
                .requires(CommanderCommandSource::hasAdmin)
                .redirect(command));
    }
}
