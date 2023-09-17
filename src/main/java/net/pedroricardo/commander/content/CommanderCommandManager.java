package net.pedroricardo.commander.content;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pedroricardo.commander.content.commands.AchievementCommand;
import net.pedroricardo.commander.content.commands.TestCommand;
import org.jetbrains.annotations.Nullable;

public class CommanderCommandManager {
    public static int SINGLE_SUCCESS = 1;
    private static final CommandDispatcher<CommanderCommandSource> DISPATCHER = new CommandDispatcher<>();

    static {
        AchievementCommand.register(DISPATCHER);
        TestCommand.register(DISPATCHER);
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

    public static void init() {
    }
}
