package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.arguments.WorldFeatureArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

@SuppressWarnings("unchecked")
public class PlaceCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.place.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<Object> command = dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("place")
                .then((RequiredArgumentBuilder) RequiredArgumentBuilder.argument("feature", WorldFeatureArgumentType.worldFeature())
                        .then((RequiredArgumentBuilder) RequiredArgumentBuilder.argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
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
                                        source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.place.success", feature.getClass().getSimpleName().substring(12), x, y, z));
                                    } else {
                                        throw FAILURE.create();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("generate")
                .redirect(command));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("gen")
                .redirect(command));
    }
}
