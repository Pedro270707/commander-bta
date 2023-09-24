package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntitySummonArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.DoubleCoordinates;

@SuppressWarnings("unchecked")
public class SummonCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("summon")
                .requires(c -> ((CommanderCommandSource)c).hasAdmin())
                .then(RequiredArgumentBuilder.argument("entity", EntitySummonArgumentType.entity())
                        .executes(c -> {
                            Vec3d coordinates = ((CommanderCommandSource)c.getSource()).getCoordinates(false);
                            if (coordinates == null) throw CommanderExceptions.notInWorld().create();

                            summonEntityAt(c, coordinates.xCoord, coordinates.yCoord - 1.6, coordinates.zCoord, 0.0f, 0.0f);

                            return CommanderCommandManager.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.argument("position", Vec3dArgumentType.vec3d())
                                .executes(c -> {
                                    DoubleCoordinates coordinates = c.getArgument("position", DoubleCoordinates.class);

                                    summonEntityAt(c, coordinates.getX(((CommanderCommandSource)c.getSource())), coordinates.getY(((CommanderCommandSource)c.getSource())), coordinates.getZ(((CommanderCommandSource)c.getSource())), 0.0f, 0.0f);

                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                }))));
    }

    private static void summonEntityAt(CommandContext<Object> c, double x, double y, double z, float yaw, float pitch) {
        Class<? extends Entity> entityClass = c.getArgument("entity", Class.class);
        Entity entity;
        try {
            entity = entityClass.getConstructor(World.class).newInstance(((CommanderCommandSource)c.getSource()).getWorld());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        entity.spawnInit();
        entity.moveTo(x, y, z, yaw, pitch);
        ((CommanderCommandSource)c.getSource()).getWorld().entityJoinedWorld(entity);
    }
}
