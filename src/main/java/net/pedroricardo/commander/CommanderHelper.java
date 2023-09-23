package net.pedroricardo.commander;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.net.command.Command;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.content.CommanderCommandManager;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CommanderHelper {
    private static Collection<Integer> IGNORABLE_KEYS = Arrays.asList(
            KeyEvent.VK_SHIFT,
            KeyEvent.VK_CONTROL,
            KeyEvent.VK_ALT,
            KeyEvent.VK_ALT_GRAPH,
            KeyEvent.VK_F1,
            KeyEvent.VK_F2,
            KeyEvent.VK_F3,
            KeyEvent.VK_F4,
            KeyEvent.VK_F5,
            KeyEvent.VK_F6,
            KeyEvent.VK_F7,
            KeyEvent.VK_F8,
            KeyEvent.VK_F9,
            KeyEvent.VK_F10,
            KeyEvent.VK_F11,
            KeyEvent.VK_F12,
            KeyEvent.VK_F14,
            KeyEvent.VK_F15,
            KeyEvent.VK_F16,
            KeyEvent.VK_F17,
            KeyEvent.VK_F18,
            KeyEvent.VK_F19,
            KeyEvent.VK_F20,
            KeyEvent.VK_F21,
            KeyEvent.VK_F22,
            KeyEvent.VK_F23,
            KeyEvent.VK_F24,
            KeyEvent.VK_PRINTSCREEN,
            KeyEvent.VK_PAUSE,
            KeyEvent.VK_HOME,
            KeyEvent.VK_PAGE_UP,
            KeyEvent.VK_PAGE_DOWN,
            KeyEvent.VK_END,
            KeyEvent.VK_NUM_LOCK,
            KeyEvent.VK_WINDOWS,
            KeyEvent.VK_STOP
    );

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

    public static boolean isIgnorableKey(int key) {
        return IGNORABLE_KEYS.contains(key);
    }

    public static CompletableFuture<Suggestions> suggest(Iterable<String> iterable, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string2 : iterable) {
            if (!matchesSubStr(string, string2.toLowerCase(Locale.ROOT))) continue;
            suggestionsBuilder.suggest(string2);
        }
        return suggestionsBuilder.buildFuture();
    }

    public static boolean matchesSubStr(String string, String string2) {
        int i = 0;
        while (!string2.startsWith(string, i)) {
            if ((i = string2.indexOf(95, i)) < 0) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static Optional<String> getStringToSuggest(String checkedString, String input) {
        if (checkedString.startsWith(input)) {
            return Optional.of(checkedString);
        } else if (checkedString.substring(checkedString.indexOf('.') + 1).startsWith(input)) {
            return Optional.of(checkedString.substring(checkedString.indexOf('.') + 1));
        }
        return Optional.empty();
    }

    public static boolean matchesKeyString(String checkedString, String input) {
        if (checkedString.equals(input)) {
            return true;
        }
        return checkedString.substring(checkedString.indexOf('.') + 1).equals(input);
    }
}
