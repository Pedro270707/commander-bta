package net.pedroricardo.commander;

import net.pedroricardo.commander.commands.CommanderCommandParameter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandParameterParser {
    public static int getParameterInCursorIndex(String message, int cursor) {
        List<String> commandParameters = CommanderHelper.getCommandParameterListWithoutSlash(message);
        return ListHelper.getStringIndexFromCharIndex(commandParameters, cursor - 1);
    }

    public static String getParameterInCursor(String message, int cursor) {
        List<String> commandParameters = CommanderHelper.getCommandParameterListWithoutSlash(message);
        if (commandParameters.isEmpty()) return "";
        return commandParameters.get(ListHelper.getStringIndexFromCharIndex(commandParameters, cursor - 1));
    }

    public static String replaceParameterOnString(String message, int index, String parameter) {
        List<String> parameterList = CommanderHelper.getCommandParameterList(message);

        if (parameterList.size() <= index) return message + parameter;

        parameterList.remove(index);
        parameterList.add(index, parameter);
        return ListHelper.join(parameterList, " ");
    }

    public static int getCharIndexInEndOfParameterOnString(String message, int index) {
        List<String> parameters = CommanderHelper.getCommandParameterList(message);
        List<String> parametersBeforeIndex = parameters.subList(0, index + 1);
        return ListHelper.join(parametersBeforeIndex, " ").length();
    }

    public static int getLocalIndex(List<CommanderCommandParameter> parameters, int index) {
        for (CommanderCommandParameter item : parameters) {
            for (int i = 0; i < item.getExpectedParameters(); i++) {
                if (index == 0) return i;
                else index -= 1;
            }
        }
        return 0;
    }

    @Nullable
    public static CommanderCommandParameter getParameterFromIndex(List<CommanderCommandParameter> parameters, int index) {
        for (CommanderCommandParameter item : parameters) {
            for (int i = 0; i < item.getExpectedParameters(); i++) {
                if (index == 0) return item;
                else index -= 1;
            }
        }
        return null;
    }
}
