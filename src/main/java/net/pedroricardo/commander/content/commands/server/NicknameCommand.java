package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.packet.Packet72UpdatePlayerProfile;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class NicknameCommand {
    private static final SimpleCommandExceptionType NICKNAME_TOO_LARGE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.nickname.exception_too_large"));
    private static final SimpleCommandExceptionType NICKNAME_TOO_SMALL = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.nickname.exception_too_small"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("nickname")
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("set")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.player())
                                .requires(CommanderCommandSource::hasAdmin)
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("nickname", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            String nickname = c.getArgument("nickname", String.class);
                                            if (nickname.length() > 16) throw NICKNAME_TOO_LARGE.create();
                                            if (nickname.isEmpty()) throw NICKNAME_TOO_SMALL.create();
                                            List<? extends Entity> entities = entitySelector.get(source);

                                            EntityPlayerMP player = (EntityPlayerMP) entities.get(0);

                                            player.nickname = nickname;
                                            player.hadNicknameSet = true;
                                            player.mcServer.playerList.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));
                                            if (source.getSender() == player) {
                                                source.sendTranslatableMessage("commands.commander.nickname.set.success", nickname);
                                            } else {
                                                source.sendTranslatableMessage("commands.commander.nickname.set.success_other", player.username, nickname);
                                                source.sendTranslatableMessage(player, "commands.commander.nickname.set.success_receiver", nickname);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("nickname", StringArgumentType.greedyString())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    String nickname = c.getArgument("nickname", String.class);
                                    if (nickname.length() > 16) throw NICKNAME_TOO_LARGE.create();
                                    if (nickname.isEmpty()) throw NICKNAME_TOO_SMALL.create();

                                    EntityPlayerMP player = (EntityPlayerMP) source.getSender();

                                    if (player == null) throw CommanderExceptions.notInWorld().create();

                                    player.nickname = nickname;
                                    player.hadNicknameSet = true;
                                    player.mcServer.playerList.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));

                                    source.sendTranslatableMessage("commands.commander.nickname.set.success", nickname);

                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("get")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.player())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                    List<? extends Entity> entities = entitySelector.get(source);
                                    EntityPlayerMP player = (EntityPlayerMP) entities.get(0);

                                    source.sendTranslatableMessage("commands.commander.nickname.get.success", player.username, player.nickname);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("reset")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.player())
                                .requires(CommanderCommandSource::hasAdmin)
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                    List<? extends Entity> entities = entitySelector.get(source);

                                    EntityPlayerMP player = (EntityPlayerMP) entities.get(0);

                                    player.nickname = "";
                                    player.hadNicknameSet = false;
                                    player.mcServer.playerList.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));
                                    if (source.getSender() == player) {
                                        source.sendTranslatableMessage("commands.commander.nickname.reset.success");
                                    } else {
                                        source.sendTranslatableMessage("commands.commander.nickname.reset.success_other", player.username);
                                        source.sendTranslatableMessage(player, "commands.commander.nickname.reset.success_receiver");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();

                            EntityPlayerMP player = (EntityPlayerMP) source.getSender();

                            if (player == null) throw CommanderExceptions.notInWorld().create();

                            player.nickname = "";
                            player.hadNicknameSet = false;
                            player.mcServer.playerList.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));

                            source.sendTranslatableMessage("commands.commander.nickname.reset.success", player.username);

                            return Command.SINGLE_SUCCESS;
                        })));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("nick")
                .redirect(command));
    }
}
