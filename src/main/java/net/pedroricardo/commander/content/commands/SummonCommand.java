package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntitySummonArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.DoubleCoordinates;

public class SummonCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("summon")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, Class<? extends Entity>>argument("entity", EntitySummonArgumentType.entity())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            Vec3d coordinates = source.getCoordinates(false);
                            if (source.getSender() == null || coordinates == null) throw CommanderExceptions.notInWorld().create();

                            Entity entity = summonEntityAt(c, coordinates.xCoord, coordinates.yCoord - source.getSender().heightOffset, coordinates.zCoord, 0.0f, 0.0f);

                            source.sendTranslatableMessage("commands.commander.summon.success_single_entity", CommanderHelper.getEntityName(entity));

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, DoubleCoordinates>argument("position", Vec3dArgumentType.vec3d())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    DoubleCoordinates coordinates = c.getArgument("position", DoubleCoordinates.class);

                                    Entity entity = summonEntityAt(c, coordinates.getX(source), coordinates.getY(source, true), coordinates.getZ(source), 0.0f, 0.0f);

                                    source.sendTranslatableMessage("commands.commander.summon.success_single_entity", CommanderHelper.getEntityName(entity));

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("amount", IntegerArgumentType.integer(1, 255))
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            DoubleCoordinates coordinates = c.getArgument("position", DoubleCoordinates.class);
                                            int amount = c.getArgument("amount", Integer.class);

                                            for (int i = 0; i < amount; i++) {
                                                summonEntityAt(c, coordinates.getX(source), coordinates.getY(source, true), coordinates.getZ(source), 0.0f, 0.0f);
                                            }

                                            source.sendTranslatableMessage("commands.commander.summon.success_multiple_entities", amount);

                                            return Command.SINGLE_SUCCESS;
                                        })))));
    }

    @SuppressWarnings({"SameParameterValue", "unchecked"})
    private static Entity summonEntityAt(CommandContext<CommanderCommandSource> c, double x, double y, double z, float yaw, float pitch) {
        Class<? extends Entity> entityClass = c.getArgument("entity", Class.class);
        Entity entity;
        try {
            entity = entityClass.getConstructor(World.class).newInstance(c.getSource().getWorld());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        entity.spawnInit();
        entity.moveTo(x, y, z, yaw, pitch);
        c.getSource().getWorld().entityJoinedWorld(entity);
        return entity;
    }
}
