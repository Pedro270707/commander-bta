package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.DoubleCoordinates;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

@SuppressWarnings("unchecked")
public class TeleportCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<Object> command = dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("teleport")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("position", Vec3dArgumentType.vec3d())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            DoubleCoordinates targetCoordinates = c.getArgument("position", DoubleCoordinates.class);

                            if (source.getSender() != null) {
                                source.getSender().moveTo(targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source), source.getSender().yRot, source.getSender().xRot);
                                source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.teleport.location.success_single_entity", source.getSender().getDisplayName(), targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source)));
                            } else {
                                throw CommanderExceptions.notInWorld().create();
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(RequiredArgumentBuilder.argument("entity", EntityArgumentType.entities())
                        .then(RequiredArgumentBuilder.argument("position", Vec3dArgumentType.vec3d())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    EntitySelector entitySelector = c.getArgument("entity", EntitySelector.class);
                                    DoubleCoordinates targetCoordinates = c.getArgument("position", DoubleCoordinates.class);

                                    List<? extends Entity> entities = entitySelector.get(source);
                                    for (Entity entity : entities) {
                                        entity.moveTo(targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source), entity.yRot, entity.xRot);
                                    }

                                    if (entities.size() == 1) source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.teleport.location.success_single_entity", CommanderHelper.getEntityName(entities.get(0)), targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source)));
                                    else if (entities.size() > 1) source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.teleport.location.success_multiple_entities", entities.size(), targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source)));

                                    return Command.SINGLE_SUCCESS;
                                }))
                .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.entity())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            EntitySelector entitySelector = c.getArgument("entity", EntitySelector.class);
                            EntitySelector targetEntitySelector = c.getArgument("target", EntitySelector.class);
                            Entity targetEntity = targetEntitySelector.get((CommanderCommandSource) c.getSource()).get(0);

                            List<? extends Entity> entities = entitySelector.get((CommanderCommandSource) c.getSource());
                            for (Entity entity : entities) {
                                entity.moveTo(targetEntity.x, targetEntity.y - targetEntity.heightOffset, targetEntity.z, entity.yRot, entity.xRot);
                            }

                            if (entities.size() == 1) source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.teleport.entity.success_single_entity", CommanderHelper.getEntityName(entities.get(0)), CommanderHelper.getEntityName(targetEntity)));
                            else if (entities.size() > 1) source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.teleport.entity.success_multiple_entities", entities.size(), CommanderHelper.getEntityName(targetEntity)));

                            return Command.SINGLE_SUCCESS;
                        }))));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("tp")
                .redirect(command));
    }
}
