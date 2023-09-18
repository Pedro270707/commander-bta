package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntitySummonArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.helpers.DoubleCoordinates;

@SuppressWarnings("unchecked")
public class SummonCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("summon")
                .requires(c -> ((CommanderCommandSource)c).hasAdmin())
                .then(RequiredArgumentBuilder.argument("entity", EntitySummonArgumentType.entity())
                        .executes(c -> {
                            Class<? extends Entity> entityClass = c.getArgument("entity", Class.class);
                            Entity entity;
                            try {
                                entity = entityClass.getConstructor(World.class).newInstance(((CommanderCommandSource)c.getSource()).getSender().world);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            entity.spawnInit();
                            entity.moveTo(((CommanderCommandSource)c.getSource()).getSender().x, ((CommanderCommandSource)c.getSource()).getSender().y, ((CommanderCommandSource)c.getSource()).getSender().z, 0.0f, 0.0f);
                            ((CommanderCommandSource)c.getSource()).getSender().world.entityJoinedWorld(entity);
                            return CommanderCommandManager.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.argument("pos", Vec3dArgumentType.vec3d())
                                .executes(c -> {
                                    Class<? extends Entity> entityClass = c.getArgument("entity", Class.class);
                                    DoubleCoordinates coordinates = c.getArgument("pos", DoubleCoordinates.class);
                                    Entity entity;
                                    try {
                                        entity = entityClass.getConstructor(World.class).newInstance(((CommanderCommandSource)c.getSource()).getSender().world);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                    entity.spawnInit();
                                    entity.moveTo(coordinates.getX(((CommanderCommandSource)c.getSource()).getSender().x), coordinates.getY(((CommanderCommandSource)c.getSource()).getSender().y), coordinates.getZ(((CommanderCommandSource)c.getSource()).getSender().z), 0.0f, 0.0f);
                                    ((CommanderCommandSource)c.getSource()).getSender().world.entityJoinedWorld(entity);
                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                }))));
    }
}
