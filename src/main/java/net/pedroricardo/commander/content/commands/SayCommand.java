package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.pedroricardo.commander.content.CommanderCommandSource;

public class SayCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("say")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("message", StringArgumentType.greedyString())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            String message = StringArgumentType.getString(c, "message");
                            String senderName = source.getName();
                            source.sendMessageToAllPlayers("[" + senderName + "§r]" + message);
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
