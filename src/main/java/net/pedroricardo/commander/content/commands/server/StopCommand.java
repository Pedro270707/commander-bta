package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.server.MinecraftServer;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.CommanderServerCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

@SuppressWarnings("unchecked")
public class StopCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("stop")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                    if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                    MinecraftServer server = ((IServerCommandSource)source).getServer();

                    source.sendMessage(I18n.getInstance().translateKey("commands.commander.stop.success"));
                    if (server.configManager != null) {
                        server.configManager.savePlayerStates();
                    }
                    for (int i = 0; i < server.worldMngr.length; ++i) {
                        server.worldMngr[i].saveWorld(true, null);
                    }
                    server.initiateShutdown();
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
