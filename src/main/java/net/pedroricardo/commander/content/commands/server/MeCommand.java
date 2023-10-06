package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

@SuppressWarnings("unchecked")
public class MeCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("me")
                .then(RequiredArgumentBuilder.argument("asterisk", BoolArgumentType.bool())
                        .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    String message = StringArgumentType.getString(c, "message");
                                    boolean asterisk = BoolArgumentType.getBool(c, "asterisk");
                                    if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();
                                    String senderName = source.getSender() == null ? "Server" : CommanderHelper.getEntityName(source.getSender());
                                    ((IServerCommandSource) source).getServer().configManager.sendEncryptedChatToAllPlayers((asterisk ? "* " : "") + senderName + message);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            String message = StringArgumentType.getString(c, "message");
                            if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();
                            String senderName = source.getSender() == null ? "Server" : CommanderHelper.getEntityName(source.getSender());
                            ((IServerCommandSource) source).getServer().configManager.sendEncryptedChatToAllPlayers(senderName + message);
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
