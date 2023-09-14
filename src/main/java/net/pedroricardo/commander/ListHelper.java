package net.pedroricardo.commander;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListHelper {
    public static int getStringIndexFromCharIndex(List<String> list, int charIndex) {
        if (charIndex < 0) return 0;
        else if (charIndex >= join(list, "").length()) return list.size() - 1;
        int charactersLeft = charIndex;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).length() >= charactersLeft) {
                return i;
            }
            charactersLeft -= list.get(i).length() + 1;
        }
        return 0;
    }

    public static String join(List<String> list) {
        return join(list, ",");
    }

    public static String join(List<String> list, @Nullable String separator) {
        if (separator == null) separator = ",";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                stringBuilder.append(separator);
            }
            stringBuilder.append(list.get(i));
        }
        return stringBuilder.toString();
    }

    public static List<String> elementsStartingWith(List<String> list, String startsWith) {
        List<String> newList = new ArrayList<>();
        for (String element : list) {
            if (element.startsWith(startsWith)) {
                newList.add(element);
            }
        }
        return newList;
    }
}
