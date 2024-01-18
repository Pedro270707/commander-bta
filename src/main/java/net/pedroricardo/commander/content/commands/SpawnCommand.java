package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class SpawnCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("spawn")
                .requires(CommanderCommandSource::hasAdmin)
                .executes(c -> {
                    CommanderCommandSource source = c.getSource();
                    EntityPlayer sender = source.getSender();
                    World world = source.getWorld(0);
                    ChunkCoordinates spawnCoordinates = world.getSpawnPoint();

                    if (sender == null) throw CommanderExceptions.notInWorld().create();

                    if (sender.dimension != 0) source.movePlayerToDimension(sender, 0);
                    sender.absMoveTo(spawnCoordinates.x + 0.5, spawnCoordinates.y, spawnCoordinates.z + 0.5, sender.yRot, sender.xRot);
                    source.sendTranslatableMessage("commands.commander.spawn.success");
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("players", EntityArgumentType.players())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            EntityPlayer sender = source.getSender();
                            World world = source.getWorld(0);
                            ChunkCoordinates spawnCoordinates = world.getSpawnPoint();
                            EntitySelector entitySelector = c.getArgument("players", EntitySelector.class);
                            List<? extends Entity> players = entitySelector.get(source);

                            for (Entity entity : players) {
                                if (((EntityPlayer)entity).dimension != 0) source.movePlayerToDimension((EntityPlayer) entity, 0);
                                entity.absMoveTo(spawnCoordinates.x + 0.5, spawnCoordinates.y, spawnCoordinates.z + 0.5, entity.yRot, entity.xRot);
                                if (entity == sender) {
                                    source.sendTranslatableMessage("commands.commander.spawn.success");
                                } else {
                                    source.sendTranslatableMessage("commands.commander.spawn.success_receiver");
                                    source.sendTranslatableMessage("commands.commander.spawn.success_other", CommanderHelper.getEntityName(entity));
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("get")
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            ChunkCoordinates spawnCoordinates = source.getWorld(0).getSpawnPoint();
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.spawn.get", spawnCoordinates.x, spawnCoordinates.y, spawnCoordinates.z));
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
