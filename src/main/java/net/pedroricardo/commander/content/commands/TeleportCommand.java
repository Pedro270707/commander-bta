package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.core.entity.Entity;
import net.pedroricardo.commander.content.CommanderCommandManager;
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
                .then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("position", Vec3dArgumentType.vec3d())
                        .executes(c -> {
                            DoubleCoordinates targetCoordinates = c.getArgument("position", DoubleCoordinates.class);

                            if (((CommanderCommandSource)c.getSource()).getSender() != null) {
                                ((CommanderCommandSource)c.getSource()).getSender().moveTo(targetCoordinates.getX((CommanderCommandSource)c.getSource()), targetCoordinates.getY(((CommanderCommandSource) c.getSource()).getCoordinates(true).yCoord), targetCoordinates.getZ((CommanderCommandSource)c.getSource()), ((CommanderCommandSource) c.getSource()).getSender().yRot, ((CommanderCommandSource) c.getSource()).getSender().xRot);
                            } else {
                                throw CommanderExceptions.notInWorld().create();
                            }

                            return CommanderCommandManager.SINGLE_SUCCESS;
                        }))
                .then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("entity", EntityArgumentType.entities())
                        .then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("position", Vec3dArgumentType.vec3d())
                                .executes(c -> {
                                    EntitySelector entitySelector = c.getArgument("entity", EntitySelector.class);
                                    DoubleCoordinates targetCoordinates = c.getArgument("position", DoubleCoordinates.class);

                                    List<? extends Entity> entities = entitySelector.get((CommanderCommandSource) c.getSource());
                                    for (Entity entity : entities) {
                                        entity.moveTo(targetCoordinates.getX((CommanderCommandSource)c.getSource()), targetCoordinates.getY(((CommanderCommandSource) c.getSource()).getCoordinates(true).yCoord), targetCoordinates.getZ((CommanderCommandSource)c.getSource()), entity.yRot, entity.xRot);
                                    }

                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                }))
                .then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("target", EntityArgumentType.entity())
                        .executes(c -> {
                            EntitySelector entitySelector = c.getArgument("entity", EntitySelector.class);
                            EntitySelector targetEntitySelector = c.getArgument("target", EntitySelector.class);
                            Entity targetEntity = targetEntitySelector.get((CommanderCommandSource) c.getSource()).get(0);

                            List<? extends Entity> entities = entitySelector.get((CommanderCommandSource) c.getSource());
                            for (Entity entity : entities) {
                                entity.moveTo(targetEntity.x, targetEntity.y - targetEntity.heightOffset, targetEntity.z, entity.yRot, entity.xRot);
                            }

                            return CommanderCommandManager.SINGLE_SUCCESS;
                        }))));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("tp")
                .redirect(command));
    }
}
