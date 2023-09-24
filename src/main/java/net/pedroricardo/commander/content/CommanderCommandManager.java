package net.pedroricardo.commander.content;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.net.command.Command;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.content.commands.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

@SuppressWarnings("unchecked")
public class CommanderCommandManager {
    public static int FAILURE = 0;
    public static int SINGLE_SUCCESS = 1;
    private static final CommandDispatcher<CommanderCommandSource> DISPATCHER = new CommandDispatcher<>();

    static {
        AchievementCommand.register(DISPATCHER);
        ClearCommand.register(DISPATCHER);
        KillCommand.register(DISPATCHER);
        SeedCommand.register(DISPATCHER);
        SetBlockCommand.register(DISPATCHER);
        SummonCommand.register(DISPATCHER);
        TeleportCommand.register(DISPATCHER);

        registerLegacyCommands();

//        TestCommand.register(DISPATCHER);
    }

    public static void execute(String s, CommanderCommandSource commandSource) throws CommandSyntaxException {
        DISPATCHER.execute(s, commandSource);
    }

    public static CommandDispatcher<CommanderCommandSource> getDispatcher() {
        return DISPATCHER;
    }

    @Nullable
    public static <S> CommandSyntaxException getParseException(ParseResults<S> parseResults) {
        if (!parseResults.getReader().canRead()) {
            return null;
        }
        if (parseResults.getExceptions().size() == 1) {
            return parseResults.getExceptions().values().iterator().next();
        }
        if (parseResults.getContext().getRange().isEmpty()) {
            return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
        }
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parseResults.getReader());
    }

    private static void registerLegacyCommands() {
        for (Command command : Commands.commands) {
            if (DISPATCHER.findNode(Collections.singletonList(command.getName())) == null) {
                DISPATCHER.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal(command.getName())
                        .then(RequiredArgumentBuilder.argument("command", StringArgumentType.greedyString())
                                .executes(c -> {
                                    Commands.getCommand(command.getName()).execute(((CommanderCommandSource) c.getSource()).getCommandHandler(), ((CommanderCommandSource) c.getSource()).getCommandSender(), c.getArgument("command", String.class).split(" "));
                                    return SINGLE_SUCCESS;
                                })));
            }
        }
    }

    public static void init() {
    }
}
