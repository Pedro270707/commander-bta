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
public class DeopCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.deop.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("deop")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("target", EntityArgumentType.players())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                            List<? extends Entity> entities = entitySelector.get(source);
                            MinecraftServer server = ((IServerCommandSource)source).getServer();

                            boolean deoppedSomeone = false;

                            for (Entity entity : entities) {
                                EntityPlayerMP player = (EntityPlayerMP) entity;
                                if (player.isOperator()) {
                                    deoppedSomeone = true;
                                    server.configManager.deopPlayer(player.username);
                                    source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.deop.success", player.username));
                                    source.sendMessageToPlayer(player, I18n.getInstance().translateKey("commands.commander.deop.success_receiver"));
                                }
                                server.configManager.sendPacketToAllPlayers(new Packet72UpdatePlayerProfile(player.username, player.nickname, player.score, player.chatColor, true, player.isOperator()));
                            }
                            if (!deoppedSomeone) {
                                throw FAILURE.create();
                            }
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
