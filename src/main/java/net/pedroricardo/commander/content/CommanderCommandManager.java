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
import net.pedroricardo.commander.content.commands.server.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

@SuppressWarnings("unchecked")
public class CommanderCommandManager {
    private final boolean isServer;

    public CommanderCommandManager(boolean isServer) {
        this.isServer = isServer;
    }

    private final CommandDispatcher<CommanderCommandSource> DISPATCHER = new CommandDispatcher<>();

    public void init() {
        AchievementCommand.register(DISPATCHER);
        ClearCommand.register(DISPATCHER);
        KillCommand.register(DISPATCHER);
        SeedCommand.register(DISPATCHER);
        SetBlockCommand.register(DISPATCHER);
        SummonCommand.register(DISPATCHER);
        TeleportCommand.register(DISPATCHER);
        MessageCommand.register(DISPATCHER);
        SetSpawnCommand.register(DISPATCHER);
        TimeCommand.register(DISPATCHER);
        GameModeCommand.register(DISPATCHER);
        WeatherCommand.register(DISPATCHER);
        SpawnCommand.register(DISPATCHER);
        PlaceCommand.register(DISPATCHER);
        HelpCommand.register(DISPATCHER);
        ChunkCommand.register(DISPATCHER);
        GiveCommand.register(DISPATCHER);

        this.registerLegacyCommands();

        if (this.isServer) {
            StopCommand.register(DISPATCHER);
            OpCommand.register(DISPATCHER);
            DeopCommand.register(DISPATCHER);
            ListCommand.register(DISPATCHER);
            DifficultyCommand.register(DISPATCHER);
        }
//        TestCommand.register(DISPATCHER);
    }

    public void execute(String s, CommanderCommandSource commandSource) throws CommandSyntaxException {
        this.DISPATCHER.execute(s, commandSource);
    }

    public CommandDispatcher<CommanderCommandSource> getDispatcher() {
        return this.DISPATCHER;
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

    private void registerLegacyCommands() {
        for (Command command : Commands.commands) {
            if (this.DISPATCHER.findNode(Collections.singletonList(command.getName())) == null) {
                this.DISPATCHER.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal(command.getName())
                        .executes(c -> {
                            Commands.getCommand(command.getName()).execute(((CommanderCommandSource) c.getSource()).getCommandHandler(), ((CommanderCommandSource) c.getSource()).getCommandSender(), new String[]{});
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.argument("command", StringArgumentType.greedyString())
                                .executes(c -> {
                                    Commands.getCommand(command.getName()).execute(((CommanderCommandSource) c.getSource()).getCommandHandler(), ((CommanderCommandSource) c.getSource()).getCommandSender(), c.getArgument("command", String.class).split(" "));
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })));
            }
        }
    }
}
