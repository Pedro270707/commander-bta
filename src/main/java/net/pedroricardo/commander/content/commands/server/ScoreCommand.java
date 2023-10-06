package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.packet.Packet72UpdatePlayerProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

@SuppressWarnings("unchecked")
public class ScoreCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("score")
                .then(LiteralArgumentBuilder.literal("get")
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            EntityPlayer player = source.getSender();
                            if (player == null) throw CommanderExceptions.notInWorld().create();
                            source.sendTranslatableMessage("commands.commander.score.get.success", player.score);
                            return player.score;
                        })
                        .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.player())
                                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                    EntityPlayer player = (EntityPlayer) entitySelector.get(source).get(0);
                                    if (player == source.getSender()) {
                                        source.sendTranslatableMessage("commands.commander.score.get.success", player.score);
                                    } else {
                                        source.sendTranslatableMessage("commands.commander.score.get.success_other", CommanderHelper.getEntityName(player), player.score);
                                    }
                                    return player.score;
                                })))
                .then(LiteralArgumentBuilder.literal("set")
                        .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                        .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.players())
                                .then(RequiredArgumentBuilder.argument("score", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                        .executes(c -> {
                                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            int score = c.getArgument("score", Integer.class);
                                            for (Entity entity : entities) {
                                                setPlayerScore(source, (EntityPlayerMP) entity, score);
                                            }
                                            return score;
                                        }))))
                .then(LiteralArgumentBuilder.literal("add")
                        .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                        .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.players())
                                .then(RequiredArgumentBuilder.argument("score", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                        .executes(c -> {
                                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            int score = c.getArgument("score", Integer.class);
                                            for (Entity entity : entities) {
                                                setPlayerScore(source, (EntityPlayerMP) entity, ((EntityPlayerMP)entity).score + score);
                                            }
                                            return score;
                                        })))));
    }

    private static void setPlayerScore(CommanderCommandSource source, EntityPlayerMP player, int score) throws CommandSyntaxException {
        if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();
        MinecraftServer server = ((IServerCommandSource) source).getServer();
        player.score = score;
        server.configManager.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));
        if (player == source.getSender()) {
            source.sendTranslatableMessage("commands.commander.score.set.success", score);
        } else {
            source.sendTranslatableMessage("commands.commander.score.set.success_other", CommanderHelper.getEntityName(player), score);
            source.sendTranslatableMessage(player, "commands.commander.score.set.success_receiver", score);
        }
    }
}
