package net.pedroricardo.commander;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.net.command.Command;
import net.minecraft.core.net.command.Commands;
import net.minecraft.core.util.helper.LogPrintStream;
import net.minecraft.core.util.helper.ReflectionHelper;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.content.CommanderCommandManager;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommanderHelper {
    public static final Map<String, Class<? extends WorldFeature>> WORLD_FEATURES = new HashMap<>();

    public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> NO_SUGGESTIONS = (builder, consumer) -> builder.buildFuture();
    public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_BLOCKS = (builder, consumer) -> {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Block block : Block.blocksList) {
            if (block == null) continue;
            CommanderHelper.getStringToSuggest(block.getKey(), remaining).ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    };

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

    public static CompletableFuture<Suggestions> suggest(String string, SuggestionsBuilder suggestionsBuilder) {
        String stringRemaining = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        if (matchesSubStr(stringRemaining, string.toLowerCase(Locale.ROOT))) suggestionsBuilder.suggest(string);
        return suggestionsBuilder.buildFuture();
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
        if (checkedString.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))) {
            return Optional.of(checkedString);
        } else {
            if (checkedString.contains(".") && checkedString.toLowerCase(Locale.ROOT).substring(checkedString.indexOf('.') + 1).startsWith(input.toLowerCase(Locale.ROOT))) {
                return Optional.of(checkedString.substring(checkedString.indexOf('.') + 1));
            }
        }
        return Optional.empty();
    }

    public static boolean matchesKeyString(String checkedString, String input) {
        if (checkedString.equals(input)) {
            return true;
        }
        return checkedString.substring(checkedString.indexOf('.') + 1).equals(input);
    }

    public static String getEntityName(Entity entity) {
        if (entity instanceof EntityLiving) {
            if (!LogPrintStream.removeColorCodes(((EntityLiving) entity).getDisplayName()).isEmpty()) {
                return ((EntityLiving) entity).getDisplayName();
            }
        }
        return EntityDispatcher.getEntityString(entity);
    }

    public static void init() {
        try {
            for (Class<?> clazz : CommanderReflectionHelper.getAllClasses(className -> className.startsWith("net.minecraft.core.world.generate.feature"))) {
                if (!WorldFeature.class.isAssignableFrom(clazz)) continue;
                WORLD_FEATURES.put(clazz.getSimpleName().substring(12), (Class<? extends WorldFeature>) clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
