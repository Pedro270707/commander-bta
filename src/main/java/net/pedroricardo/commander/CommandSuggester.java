package net.pedroricardo.commander;

import net.minecraft.client.Minecraft;
import net.minecraft.core.net.command.Command;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.commands.CommanderCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandSuggester {
    public static List<String> getSuggestedCommands(String string) {
        if (string.trim().equals("")) return new ArrayList<>();
        String command = string.trim().substring(1).split(" ")[0];
        List<String> suggestedCommands = new ArrayList<>();

        for (Command currentCommand : Commands.commands) {
            for (String currentCommandName : currentCommand.getNames()) {
                if (currentCommandName.startsWith(command)) {
                    suggestedCommands.add(currentCommandName);
                }
            }
        }

        return suggestedCommands;
    }

    public static List<String> getCommandSuggestions(Minecraft mc, int parameterIndex, String parameter, Command command) {
        List<String> list;
        if (!(command instanceof CommanderCommand)) {
            return getDefaultSuggestions(mc, parameter);
        } else {
            list = ((CommanderCommand)command).getCommandSuggestions(mc, parameterIndex, parameter);
        }
        return list;
    }

    public static List<String> getDefaultSuggestions(Minecraft mc, String parameter) {
        List<String> list = new ArrayList<>();
        mc.theWorld.players.forEach(player -> {
            if (player.username.startsWith(parameter)) {
                list.add(player.username);
            }
        });
        if (parameter.length() >= 6 && "Herobrine".startsWith(parameter) && !list.contains("Herobrine")) {
            list.add("Herobrine");
        }
        return list;
    }
}
