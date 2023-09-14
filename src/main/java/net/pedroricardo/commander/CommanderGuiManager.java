package net.pedroricardo.commander;

import java.util.ArrayList;
import java.util.List;

public class CommanderGuiManager {
    public static int commandIndex = 0;
    public static String tablessMessage = "";
    public static int cursor = -1;
    public static List<String> suggestions = new ArrayList<>();
    public static String currentError = "";
    public static int scroll = 0;

    public static boolean scroll(int amount) {
        if (CommanderGuiManager.scroll + amount >= 0 && CommanderGuiManager.scroll + amount <= CommanderGuiManager.suggestions.size() - Commander.maxSuggestions) {
            CommanderGuiManager.scroll += amount;
            return true;
        }
        return false;
    }
}
