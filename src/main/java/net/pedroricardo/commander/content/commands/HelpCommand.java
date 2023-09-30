package net.pedroricardo.commander.content.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.content.CommanderCommandSource;

import java.util.Map;

@SuppressWarnings("unchecked")
public class HelpCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.help.failed"));

    public static void register(CommandDispatcher<CommanderCommandSource> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("help").executes(c -> {
            Map<CommandNode<CommanderCommandSource>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(), (CommanderCommandSource)c.getSource());
            for (String string : map.values()) {
                ((CommanderCommandSource)c.getSource()).sendMessage("/" + string);
            }
            return map.size();
        })).then(RequiredArgumentBuilder.argument("command", StringArgumentType.greedyString()).executes(commandContext -> {
            ParseResults<CommanderCommandSource> parseResults = commandDispatcher.parse(StringArgumentType.getString(commandContext, "command"), (CommanderCommandSource)commandContext.getSource());
            if (parseResults.getContext().getNodes().isEmpty()) {
                throw FAILURE.create();
            }
            Map<CommandNode<CommanderCommandSource>, String> map = commandDispatcher.getSmartUsage(Iterables.getLast(parseResults.getContext().getNodes()).getNode(), (CommanderCommandSource)commandContext.getSource());
            for (String string : map.values()) {
                ((CommanderCommandSource)commandContext.getSource()).sendMessage("/" + parseResults.getReader().getString() + " " + string);
            }
            return map.size();
        })));
    }
}
