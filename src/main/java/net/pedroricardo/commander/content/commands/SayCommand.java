package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.net.packet.Packet3Chat;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

public class SayCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("say")
                .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("message", StringArgumentType.greedyString())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            String message = StringArgumentType.getString(c, "message");
                            if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();
                            String senderName = source.getName();
                            ((IServerCommandSource) source).getServer().playerList.sendPacketToAllPlayers(new Packet3Chat("[" + senderName + "§r]" + message));
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
