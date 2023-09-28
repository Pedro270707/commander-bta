package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.arguments.WorldFeatureArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

import java.util.Collections;

@SuppressWarnings("unchecked")
public class PlaceCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<Object> command = dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("place")
                .then((RequiredArgumentBuilder) RequiredArgumentBuilder.argument("feature", WorldFeatureArgumentType.worldFeature())
                    .executes(c -> {
                        CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                        WorldFeature feature = c.getArgument("feature", WorldFeature.class);
                        Vec3d coordinates = ((CommanderCommandSource) c.getSource()).getBlockCoordinates();

                        if (coordinates == null) throw CommanderExceptions.notInWorld().create();

                        feature.generate(source.getWorld(), source.getWorld().rand, (int)coordinates.xCoord, (int)coordinates.yCoord, (int)coordinates.zCoord);
                        return Command.SINGLE_SUCCESS;
                    })));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("generate")
                .redirect(command));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("gen")
                .redirect(command));
    }
}
