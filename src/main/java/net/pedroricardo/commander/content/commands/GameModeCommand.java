package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.gamemode.Gamemode;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.GameModeArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

@SuppressWarnings("unchecked")
public class GameModeCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("gamemode")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("gamemode", GameModeArgumentType.gameMode())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            Gamemode gameMode = c.getArgument("gamemode", Gamemode.class);

                            if (source.getSender() == null) {
                                throw CommanderExceptions.notInWorld().create();
                            }

                            source.getSender().setGamemode(gameMode);
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.gamemode.success_self", I18n.getInstance().translateKey(gameMode.languageKey + ".name")));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.argument("targets", EntityArgumentType.players())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    Gamemode gameMode = c.getArgument("gamemode", Gamemode.class);
                                    EntitySelector entitySelector = c.getArgument("targets", EntitySelector.class);

                                    List<? extends Entity> entities = entitySelector.get((CommanderCommandSource)c.getSource());

                                    for (Entity entity : entities) {
                                        ((EntityPlayer)entity).setGamemode(gameMode);
                                        if (entity != source.getSender()) {
                                            source.sendMessageToPlayer((EntityPlayer) entity, I18n.getInstance().translateKey("commands.commander.gamemode.success_receiver"));
                                        }
                                    }

                                    if (entities.size() == 1) {
                                        if (entities.get(0) == source.getSender()) {
                                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.gamemode.success_self", I18n.getInstance().translateKey(gameMode.languageKey + ".name")));
                                        } else {
                                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.gamemode.success_other", CommanderHelper.getEntityName(entities.get(0)), I18n.getInstance().translateKey(gameMode.languageKey + ".name")));
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
        );
    }
}