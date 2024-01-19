package net.pedroricardo.commander.content;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.net.command.Command;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.content.commands.*;
import net.pedroricardo.commander.content.commands.server.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unused")
public class CommanderCommandManager {
    private final boolean isServer;

    public CommanderCommandManager(boolean isServer) {
        this.isServer = isServer;
    }

    private final CommandDispatcher<CommanderCommandSource> DISPATCHER = new CommandDispatcher<>();
    private static final Collection<CommandRegistry> externalCommands = new ArrayList<>();
    private static final Collection<CommandRegistry> externalServerCommands = new ArrayList<>();

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
        GameRuleCommand.register(DISPATCHER);
        FillCommand.register(DISPATCHER);
        CloneCommand.register(DISPATCHER);
        BiomeCommand.register(DISPATCHER);

        if (this.isServer) {
            StopCommand.register(DISPATCHER);
            OpCommand.register(DISPATCHER);
            DeopCommand.register(DISPATCHER);
            ListCommand.register(DISPATCHER);
            DifficultyCommand.register(DISPATCHER);
            ColorCommand.register(DISPATCHER);
            NicknameCommand.register(DISPATCHER);
            WhoIsCommand.register(DISPATCHER);
            ScoreCommand.register(DISPATCHER);
            MeCommand.register(DISPATCHER);
            EmotesCommand.register(DISPATCHER);
            for (CommandRegistry registry : externalServerCommands) {
                registry.register(DISPATCHER);
            }
        }

        for (CommandRegistry registry : externalCommands) {
            registry.register(DISPATCHER);
        }
        this.registerLegacyCommands();
    }

    public int execute(String s, CommanderCommandSource commandSource) throws CommandSyntaxException {
        return this.DISPATCHER.execute(s, commandSource);
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

    @SuppressWarnings("deprecation")
    private void registerLegacyCommands() {
        for (Command command : Commands.commands) {
            if (this.DISPATCHER.findNode(Collections.singletonList(command.getName())) == null) {
                CommandNode<CommanderCommandSource> registeredCommand = this.DISPATCHER.register(LiteralArgumentBuilder.<CommanderCommandSource>literal(command.getName())
                        .executes(c -> {
                            Commands.getCommand(command.getName()).execute(c.getSource().getCommandHandler(), c.getSource().getCommandSender(), new String[]{});
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("command", StringArgumentType.greedyString())
                                .executes(c -> {
                                    Commands.getCommand(command.getName()).execute(c.getSource().getCommandHandler(), c.getSource().getCommandSender(), c.getArgument("command", String.class).split(" "));
                                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                                })));
                for (String alias : command.getNames().subList(1, command.getNames().size())) {
                    if (this.DISPATCHER.findNode(Collections.singletonList(alias)) == null) {
                        this.DISPATCHER.register(LiteralArgumentBuilder.<CommanderCommandSource>literal(alias)
                                .redirect(registeredCommand));
                    }
                }
            }
        }
    }

    public static void registerCommand(CommandRegistry registry) {
        externalCommands.add(registry);
    }

    public static void registerServerCommand(CommandRegistry registry) {
        externalServerCommands.add(registry);
    }

    @FunctionalInterface
    public interface CommandRegistry {
        void register(CommandDispatcher<CommanderCommandSource> dispatcher);
    }
}
