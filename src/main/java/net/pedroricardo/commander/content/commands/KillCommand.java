package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BlockArgumentType;
import net.pedroricardo.commander.content.arguments.BlockCoordinatesArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.BlockCoordinates;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
public class KillCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("kill")
                .executes(c -> {
                    EntityPlayer sender = ((CommanderCommandSource)c.getSource()).getSender();
                    if (sender != null) {
                        sender.killPlayer();
                    }
                    return CommanderCommandManager.SINGLE_SUCCESS;
                })
                .then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("entities", EntityArgumentType.entities())
                        .executes(c -> {
                            EntitySelector entitySelector = c.getArgument("entities", EntitySelector.class);
                            List<? extends Entity> entities = entitySelector.get((CommanderCommandSource)c.getSource());
                            while (!entities.isEmpty()) {
                                if (entities.get(0) instanceof EntityLiving) {
                                    ((EntityLiving)entities.get(0)).hurt(null, 1000, null);
                                } else {
                                    entities.get(0).remove();
                                }
                            }
                            return CommanderCommandManager.SINGLE_SUCCESS;
                        })));
    }
}
