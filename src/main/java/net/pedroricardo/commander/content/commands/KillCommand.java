package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BlockArgumentType;
import net.pedroricardo.commander.content.arguments.BlockCoordinatesArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.BlockCoordinates;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class KillCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("kill")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    EntityPlayer sender = ((CommanderCommandSource)c.getSource()).getSender();

                    if (sender == null) throw CommanderExceptions.notInWorld().create();

                    sender.killPlayer();

                    return CommanderCommandManager.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.argument("entities", EntityArgumentType.entities())
                        .executes(c -> {
                            EntitySelector entitySelector = c.getArgument("entities", EntitySelector.class);
                            CopyOnWriteArrayList<? extends Entity> entities = new CopyOnWriteArrayList<>(entitySelector.get((CommanderCommandSource)c.getSource()));
                            for (Entity entity : entities) {
                                if (entity instanceof EntityPlayer) {
                                    ((EntityPlayer) entity).killPlayer();
                                } else if (entity instanceof EntityLiving) {
                                    entity.hurt(null, 100, null);
                                } else {
                                    entity.remove();
                                }
                            }
                            return CommanderCommandManager.SINGLE_SUCCESS;
                        })));
    }
}
