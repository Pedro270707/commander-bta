package net.pedroricardo.commander.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.core.net.command.Command;
import net.pedroricardo.commander.CommandParameterParser;
import net.pedroricardo.commander.ListHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class CommanderCommand extends Command {
    private final List<CommanderCommandParameterType> commandParameters = new ArrayList<>();

    public CommanderCommand(String name, String... alts) {
        super(name, alts);
    }

    public CommanderCommand withParameter(CommanderCommandParameterType parameter) {
        this.commandParameters.add(parameter);
        return this;
    }

    public List<CommanderCommandParameterType> getCommandParameters() {
        return this.commandParameters;
    }

    public List<String> getCommandSuggestions(Minecraft mc, int parameterIndex, String parameterString) {
        CommanderCommandParameterType parameter = CommandParameterParser.getParameterFromIndex(this.getCommandParameters(), parameterIndex - 1);
        if (parameter == null) return new ArrayList<>();
        int localParameterIndex = CommandParameterParser.getLocalIndex(this.getCommandParameters(), parameterIndex - 1);
        return ListHelper.elementsStartingWith(parameter.getSuggestions(mc, parameterIndex, localParameterIndex, parameterString), parameterString);
    }
}
