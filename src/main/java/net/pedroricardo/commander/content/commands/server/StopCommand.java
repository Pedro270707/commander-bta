package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.MinecraftServer;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

public class StopCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("stop")
                .requires(CommanderCommandSource::hasAdmin)
                .executes(c -> {
                    CommanderCommandSource source = c.getSource();
                    if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                    MinecraftServer server = ((IServerCommandSource)source).getServer();

                    source.sendTranslatableMessage("commands.commander.stop.success");
                    if (server.playerList != null) {
                        server.playerList.savePlayerStates();
                    }
                    for (int i = 0; i < server.dimensionWorlds.length; ++i) {
                        server.dimensionWorlds[i].saveWorld(true, null, true);
                    }
                    server.initiateShutdown();
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
