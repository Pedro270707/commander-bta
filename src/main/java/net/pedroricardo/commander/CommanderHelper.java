package net.pedroricardo.commander;

import net.minecraft.client.render.FontRenderer;
import net.minecraft.core.util.collection.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommanderHelper {
    private static final List<Pair<String, String>> delimiters = new ArrayList<>();

    static {
        delimiters.add(Pair.of("[", "]"));
        delimiters.add(Pair.of("{", "}"));
    }

    public static boolean checkForUnmatchedCharacters(String string, int i, @Nullable String currentString, @Nullable String currentImportantDelimiter) {
        if (currentString != null && !currentString.isEmpty()) {
            if ((!string.substring(i).contains(currentString))) {
                return true;
            }
        }
        if (currentImportantDelimiter != null && !currentImportantDelimiter.isEmpty()) {
            for (Pair<String, String> delimiter : delimiters) {
                if (currentImportantDelimiter.equals(delimiter.getLeft()) && !string.substring(i).contains(delimiter.getRight())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getCommandParameterList(String message) {
        List<String> parameters = new ArrayList<>();
        String currentString = null;
        String currentImportantDelimiter = null;
        int lastParameterIndex = 0;
        String trimmedMessage = message.trim();

        for (int i = 0; i < trimmedMessage.length(); i++) {
            if ((trimmedMessage.charAt(i) == '"' || trimmedMessage.charAt(i) == '\'') && trimmedMessage.charAt(i - 1) != '\\') {
                if (currentString == null) {
                    currentString = String.valueOf(trimmedMessage.charAt(i));
                } else if (currentString.equals(String.valueOf(trimmedMessage.charAt(i)))) {
                    currentString = null;
                }
            }

            if (currentString == null)
                for (Pair<String, String> delimiter : delimiters) {
                    if (String.valueOf(trimmedMessage.charAt(i)).equals(delimiter.getLeft()) && currentImportantDelimiter == null) {
                        currentImportantDelimiter = String.valueOf(trimmedMessage.charAt(i));
                    } else if (String.valueOf(trimmedMessage.charAt(i)).equals(delimiter.getRight()) && (currentImportantDelimiter != null && currentImportantDelimiter.equals(delimiter.getLeft()))) {
                        currentImportantDelimiter = null;
                    }
                }

            // If the character is a space and is not between delimiters (or is after an unmatched delimiter) and the character after it is not a space (so that the space is also included in the parameter in case there are two spaces) or, instead of all those checks, it's the last character, add this as a parameter.
            if (trimmedMessage.charAt(i) == ' ' && (currentImportantDelimiter == null && currentString == null || checkForUnmatchedCharacters(trimmedMessage, i, currentString, currentImportantDelimiter)) && trimmedMessage.charAt(i + 1) != ' ' || i == trimmedMessage.length() - 1) {
                parameters.add(trimmedMessage.substring(lastParameterIndex, i + (i == trimmedMessage.length() - 1 ? 1 : 0)));
                lastParameterIndex = i + 1;
            }
        }

        if (message.endsWith(" ")) parameters.add("");

        return parameters;
    }

    public static List<String> getCommandParameterListWithoutSlash(String message) {
        List<String> parameters = getCommandParameterList(message);

        // Remove the leading slash from the first parameter
        if (parameters.size() > 0 && parameters.get(0).startsWith("/")) {
            String firstParameter = parameters.get(0);
            parameters.remove(0);
            parameters.add(0, firstParameter.substring(1));
        }

        return parameters;
    }

    public static int getLeftMarginForSuggestions(FontRenderer fontRenderer, String message, int cursor) {
        int parameterInCursor = CommandParameterParser.getParameterInCursorIndex(message, cursor);
        return fontRenderer.getStringWidth(message.startsWith("/") ? (parameterInCursor == 0 ? "/" : "/ ") : "" + ListHelper.join(getCommandParameterListWithoutSlash(message).subList(0, parameterInCursor), " "));
    }

    public static int getLeftMarginForSuggestionsWithParameterIndex(FontRenderer fontRenderer, String message, int index) {
        List<String> commandParameters = CommanderHelper.getCommandParameterList(message);
        return fontRenderer.getStringWidth((message.startsWith("/") && index == 0 ? "/" : "") + (index == 0 ? "" : " ") + ListHelper.join(commandParameters.subList(0, index), " "));
    }

    public static String addToIndex(String originalString, String stringToAdd, int index) {
        if (index >= originalString.length()) return originalString + stringToAdd;
        return originalString.substring(0, index) +
                stringToAdd +
                originalString.substring(index);
    }
}
