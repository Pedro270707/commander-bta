package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.lang.I18n;
import net.minecraft.server.MinecraftServer;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

@SuppressWarnings("unchecked")
public class ListCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.list.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("list")
                .executes(c -> {
                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                    if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                    MinecraftServer server = ((IServerCommandSource)source).getServer();

                    int playerCount = server.playerList.playerEntities.size();
                    if (playerCount < 100) {
                        if (playerCount == 0) throw FAILURE.create();
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < server.playerList.playerEntities.size(); ++i) {
                            if (i > 0) {
                                builder.append(", ");
                            }
                            builder.append(CommanderHelper.getEntityName(server.playerList.playerEntities.get(i)));
                        }
                        if (playerCount == 1) {
                            source.sendTranslatableMessage("commands.commander.list.success_single", playerCount, builder.toString());
                        } else {
                            source.sendTranslatableMessage("commands.commander.list.success_multiple", playerCount, builder.toString());
                        }
                    } else {
                        source.sendTranslatableMessage("commands.commander.list.success_too_long", playerCount);
                    }

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
