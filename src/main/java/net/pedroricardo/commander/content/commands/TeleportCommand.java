package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.PositionArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.DoublePos;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class TeleportCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("teleport")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, DoublePos>argument("position", PositionArgumentType.pos())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            DoublePos targetCoordinates = c.getArgument("position", DoublePos.class);

                            if (source.getSender() != null) {
                                source.getSender().moveTo(targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source), source.getSender().yRot, source.getSender().xRot);
                                source.sendTranslatableMessage("commands.commander.teleport.location.success_single_entity", source.getSender().getDisplayName(), targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source));
                            } else {
                                throw CommanderExceptions.notInWorld().create();
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("entity", EntityArgumentType.entities())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, DoublePos>argument("position", PositionArgumentType.pos())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    EntitySelector entitySelector = c.getArgument("entity", EntitySelector.class);
                                    DoublePos targetCoordinates = c.getArgument("position", DoublePos.class);

                                    List<? extends Entity> entities = entitySelector.get(source);
                                    for (Entity entity : entities) {
                                        entity.moveTo(targetCoordinates.getX(source), targetCoordinates.getY(source, true), targetCoordinates.getZ(source), entity.yRot, entity.xRot);
                                    }

                                    double x = targetCoordinates.getX(source);
                                    double y = targetCoordinates.getY(source, true);
                                    double z = targetCoordinates.getZ(source);

                                    if (entities.size() == 1) source.sendTranslatableMessage("commands.commander.teleport.location.success_single_entity", CommanderHelper.getEntityName(entities.get(0)), x, y, z);
                                    else if (entities.size() > 1) source.sendTranslatableMessage("commands.commander.teleport.location.success_multiple_entities", entities.size(), x, y, z);

                                    return Command.SINGLE_SUCCESS;
                                }))
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.entity())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            EntitySelector entitySelector = c.getArgument("entity", EntitySelector.class);
                            EntitySelector targetEntitySelector = c.getArgument("target", EntitySelector.class);
                            Entity targetEntity = targetEntitySelector.get(c.getSource()).get(0);

                            List<? extends Entity> entities = entitySelector.get(c.getSource());
                            for (Entity entity : entities) {
                                entity.moveTo(targetEntity.x, targetEntity.y - targetEntity.heightOffset, targetEntity.z, entity.yRot, entity.xRot);
                            }

                            if (entities.size() == 1) source.sendTranslatableMessage("commands.commander.teleport.entity.success_single_entity", CommanderHelper.getEntityName(entities.get(0)), CommanderHelper.getEntityName(targetEntity));
                            else if (entities.size() > 1) source.sendTranslatableMessage("commands.commander.teleport.entity.success_multiple_entities", entities.size(), CommanderHelper.getEntityName(targetEntity));

                            return Command.SINGLE_SUCCESS;
                        }))));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("tp")
                .requires(CommanderCommandSource::hasAdmin)
                .redirect(command));
    }
}
