package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntitySummonArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.DoubleCoordinates;

@SuppressWarnings("unchecked")
public class SummonCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("summon")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("entity", EntitySummonArgumentType.entity())
                        .executes(c -> {
                            Vec3d coordinates = ((CommanderCommandSource)c.getSource()).getCoordinates(false);
                            if (coordinates == null) throw CommanderExceptions.notInWorld().create();

                            Entity entity = summonEntityAt(c, coordinates.xCoord, coordinates.yCoord - ((CommanderCommandSource)c.getSource()).getSender().heightOffset, coordinates.zCoord, 0.0f, 0.0f);

                            ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.summon.success_single_entity", CommanderHelper.getEntityName(entity)));

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.argument("position", Vec3dArgumentType.vec3d())
                                .executes(c -> {
                                    DoubleCoordinates coordinates = c.getArgument("position", DoubleCoordinates.class);

                                    Entity entity = summonEntityAt(c, coordinates.getX(((CommanderCommandSource)c.getSource())), coordinates.getY(((CommanderCommandSource)c.getSource()), true), coordinates.getZ(((CommanderCommandSource)c.getSource())), 0.0f, 0.0f);

                                    ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.summon.success_single_entity", CommanderHelper.getEntityName(entity)));

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer(1, 255))
                                        .executes(c -> {
                                            DoubleCoordinates coordinates = c.getArgument("position", DoubleCoordinates.class);
                                            int amount = c.getArgument("amount", Integer.class);

                                            for (int i = 0; i < amount; i++) {
                                                summonEntityAt(c, coordinates.getX(((CommanderCommandSource) c.getSource())), coordinates.getY(((CommanderCommandSource) c.getSource()), true), coordinates.getZ(((CommanderCommandSource) c.getSource())), 0.0f, 0.0f);
                                            }

                                            ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.summon.success_multiple_entities", amount));

                                            return Command.SINGLE_SUCCESS;
                                        })))));
    }

    private static Entity summonEntityAt(CommandContext<Object> c, double x, double y, double z, float yaw, float pitch) {
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
        return entity;
    }
}
