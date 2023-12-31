package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.AchievementArgumentType;

@SuppressWarnings("unchecked")
public class TestCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("test"))
                .then(((LiteralArgumentBuilder)LiteralArgumentBuilder.<CommanderCommandSource>literal("grant")
                ).then(RequiredArgumentBuilder.argument("achievement", AchievementArgumentType.achievement())
                        .then(RequiredArgumentBuilder.argument("achievement1", AchievementArgumentType.achievement())
                                .then(RequiredArgumentBuilder.argument("achievement2", AchievementArgumentType.achievement())
                                        .then(RequiredArgumentBuilder.argument("achievement3", AchievementArgumentType.achievement())
                                                .then(RequiredArgumentBuilder.argument("achievement4", AchievementArgumentType.achievement())
                                                        .then(RequiredArgumentBuilder.argument("achievement5", AchievementArgumentType.achievement())
                                                                .executes(c -> {
                                                                    ((CommanderCommandSource)c.getSource()).getSender().addChatMessage("Command run!");
                                                                    return Command.SINGLE_SUCCESS;
                                                                }))))))))));
    }
}
