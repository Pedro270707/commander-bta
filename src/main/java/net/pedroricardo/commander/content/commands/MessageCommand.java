package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.helper.LogPrintStream;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class MessageCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("message")
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("targets", EntityArgumentType.players())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    EntitySelector entitySelector = c.getArgument("targets", EntitySelector.class);
                                    String message = c.getArgument("message", String.class);

                                    String senderName = source.getName();

                                    List<? extends Entity> players = entitySelector.get(source);

                                    for (Entity player : players) {
                                        source.sendMessage(TextFormatting.LIGHT_GRAY.toString() + TextFormatting.ITALIC.toString() + I18n.getInstance().translateKeyAndFormat("commands.commander.message.outgoing", LogPrintStream.removeColorCodes(((EntityPlayer)player).getDisplayName()), message));
                                        source.sendMessage((EntityPlayer)player, TextFormatting.LIGHT_GRAY + (TextFormatting.ITALIC + I18n.getInstance().translateKeyAndFormat("commands.commander.message.incoming", senderName, message)));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("msg")
                .redirect(command));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("whisper")
                .redirect(command));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("tell")
                .redirect(command));
    }
}
