package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.Dimension;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.DimensionArgumentType;
import net.pedroricardo.commander.content.arguments.PositionArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.helpers.DoublePos;
import org.jetbrains.annotations.Nullable;

public class SpawnParticleCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("spawnparticle")
                .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("particle", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, DoublePos>argument("position", PositionArgumentType.pos())
                                .executes(c -> {
                                    execute(c, c.getArgument("particle", String.class), c.getArgument("position", DoublePos.class), Vec3d.createVector(0.0, 0.0, 0.0), null, null);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Vec3d>argument("motion", Vec3dArgumentType.vec3d())
                                        .executes(c -> {
                                            execute(c, c.getArgument("particle", String.class), c.getArgument("position", DoublePos.class), c.getArgument("motion", Vec3d.class), null, null);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Double>argument("maxDistance", DoubleArgumentType.doubleArg(0.0))
                                                .executes(c -> {
                                                    execute(c, c.getArgument("particle", String.class), c.getArgument("position", DoublePos.class), c.getArgument("motion", Vec3d.class), c.getArgument("maxDistance", Double.class), null);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Dimension>argument("dimension", DimensionArgumentType.dimension())
                                                        .executes(c -> {
                                                            execute(c, c.getArgument("particle", String.class), c.getArgument("position", DoublePos.class), c.getArgument("motion", Vec3d.class), c.getArgument("maxDistance", Double.class), c.getArgument("dimension", Dimension.class));
                                                            return Command.SINGLE_SUCCESS;
                                                        })))))));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("particle")
                .redirect(command));
    }

    private static void execute(CommandContext<CommanderCommandSource> c, String particle, DoublePos position, Vec3d motion, @Nullable Double maxDistance, @Nullable Dimension dimension) throws CommandSyntaxException {
        double x = position.getX(c.getSource());
        double y = position.getY(c.getSource(), true);
        double z = position.getZ(c.getSource());
        if (dimension != null) {
            c.getSource().addParticle(particle, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord, maxDistance, dimension.id);
        } else {
            c.getSource().addParticle(particle, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord, maxDistance);
        }
        c.getSource().sendTranslatableMessage("commands.commander.spawnparticle.success", particle, x, y, z);
    }
}
