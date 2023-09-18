package net.pedroricardo.commander;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.core.net.command.Command;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.content.CommanderCommandManager;

import java.util.ArrayList;
import java.util.List;

public class CommanderHelper {
    public static List<Suggestion> getLegacySuggestionList(String message, int cursor) {
        List<Suggestion> list = new ArrayList<>();
        String textBeforeCursor = message.substring(0, cursor);
        if (textBeforeCursor.contains("/")) {
            for (Command command : Commands.commands) {
                List<String> path = new ArrayList<>();
                path.add(command.getName());
                if (CommanderCommandManager.getDispatcher().findNode(path) == null && command.getName().startsWith(textBeforeCursor.substring(1)) && !command.getName().equals(textBeforeCursor.substring(1))) {
                    list.add(new Suggestion(new StringRange(1, 1 + command.getName().length()), command.getName()));
                }
            }
        }
        return list;
    }
}
