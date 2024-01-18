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
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

import java.util.List;

public class GameModeCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("gamemode")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, Gamemode>argument("gamemode", GameModeArgumentType.gameMode())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            Gamemode gameMode = c.getArgument("gamemode", Gamemode.class);

                            if (source.getSender() == null) {
                                throw CommanderExceptions.notInWorld().create();
                            }

                            source.getSender().setGamemode(gameMode);
                            source.sendTranslatableMessage("commands.commander.gamemode.success_self", I18n.getInstance().translateKey(gameMode.getLanguageKey() + ".name"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("targets", EntityArgumentType.players())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    Gamemode gameMode = c.getArgument("gamemode", Gamemode.class);
                                    EntitySelector entitySelector = c.getArgument("targets", EntitySelector.class);

                                    List<? extends Entity> entities = entitySelector.get(c.getSource());

                                    for (Entity entity : entities) {
                                        ((EntityPlayer)entity).setGamemode(gameMode);
                                        if (entity != source.getSender()) {
                                            source.sendTranslatableMessage((EntityPlayer) entity, "commands.commander.gamemode.success_receiver");
                                        }
                                    }

                                    if (entities.size() == 1) {
                                        if (entities.get(0) == source.getSender()) {
                                            source.sendTranslatableMessage("commands.commander.gamemode.success_self", I18n.getInstance().translateKey(gameMode.getLanguageKey() + ".name"));
                                        } else {
                                            source.sendTranslatableMessage("commands.commander.gamemode.success_other", CommanderHelper.getEntityName(entities.get(0)), I18n.getInstance().translateKey(gameMode.getLanguageKey() + ".name"));
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
        );
    }
}
