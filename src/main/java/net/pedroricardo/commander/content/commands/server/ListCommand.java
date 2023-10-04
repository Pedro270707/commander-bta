package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.packet.Packet72UpdatePlayerProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

@SuppressWarnings("unchecked")
public class ListCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.list.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("list")
                .executes(c -> {
                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                    if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                    MinecraftServer server = ((IServerCommandSource)source).getServer();

                    int playerCount = server.configManager.playerEntities.size();
                    String text;
                    if (playerCount < 100) {
                        if (playerCount == 0) throw FAILURE.create();
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < server.configManager.playerEntities.size(); ++i) {
                            if (i > 0) {
                                builder.append(", ");
                            }
                            builder.append(server.configManager.playerEntities.get(i).getDisplayName());
                        }
                        if (playerCount == 1) {
                            text = I18n.getInstance().translateKeyAndFormat("commands.commander.list.success_single", playerCount, builder.toString());
                        } else {
                            text = I18n.getInstance().translateKeyAndFormat("commands.commander.list.success_multiple", playerCount, builder.toString());
                        }
                    } else {
                        text = I18n.getInstance().translateKeyAndFormat("commands.commander.list.success_too_long", playerCount);
                    }
                    source.sendMessage(text);

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
