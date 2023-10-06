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

@SuppressWarnings("unchecked")
public class NicknameCommand {
    private static final SimpleCommandExceptionType NICKNAME_TOO_LARGE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.nickname.exception_too_large"));
    private static final SimpleCommandExceptionType NICKNAME_TOO_SMALL = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.nickname.exception_too_small"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<Object> command = dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("nickname")
                .then(LiteralArgumentBuilder.literal("set")
                        .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.player())
                                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                                .then(RequiredArgumentBuilder.argument("nickname", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            String nickname = c.getArgument("nickname", String.class);
                                            if (nickname.length() > 16) throw NICKNAME_TOO_LARGE.create();
                                            if (nickname.length() < 1) throw NICKNAME_TOO_SMALL.create();
                                            List<? extends Entity> entities = entitySelector.get(source);

                                            EntityPlayerMP player = (EntityPlayerMP) entities.get(0);

                                            player.nickname = nickname;
                                            player.hadNicknameSet = true;
                                            player.mcServer.configManager.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));
                                            if (source.getSender() == player) {
                                                source.sendTranslatableMessage("commands.commander.nickname.set.success", nickname);
                                            } else {
                                                source.sendTranslatableMessage("commands.commander.nickname.set.success_other", player.username, nickname);
                                                source.sendTranslatableMessage(player, "commands.commander.nickname.set.success_receiver", nickname);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(RequiredArgumentBuilder.argument("nickname", StringArgumentType.greedyString())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    String nickname = c.getArgument("nickname", String.class);
                                    if (nickname.length() > 16) throw NICKNAME_TOO_LARGE.create();
                                    if (nickname.length() < 1) throw NICKNAME_TOO_SMALL.create();

                                    EntityPlayerMP player = (EntityPlayerMP) source.getSender();

                                    if (player == null) throw CommanderExceptions.notInWorld().create();

                                    player.nickname = nickname;
                                    player.hadNicknameSet = true;
                                    player.mcServer.configManager.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));
                                    if (source.getSender() == player) {
                                        source.sendTranslatableMessage("commands.commander.nickname.set.success", nickname);
                                    } else {
                                        source.sendTranslatableMessage("commands.commander.nickname.set.success_other", player.username, nickname);
                                        source.sendTranslatableMessage(player, "commands.commander.nickname.set.success_receiver", nickname);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.literal("get")
                        .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.player())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                    List<? extends Entity> entities = entitySelector.get(source);
                                    EntityPlayerMP player = (EntityPlayerMP) entities.get(0);

                                    source.sendTranslatableMessage("commands.commander.nickname.get.success", player.username, player.nickname);
                                    return Command.SINGLE_SUCCESS;
                                }))));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("nick")
                .redirect(command));
    }
}
