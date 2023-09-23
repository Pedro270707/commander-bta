package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.text.Text;
import net.minecraft.core.lang.text.TextTranslatable;
import net.minecraft.core.player.gamemode.Gamemode;
import net.pedroricardo.commander.CommanderHelper;

import java.util.*;
import java.util.function.Predicate;

public class EntitySelectorOptions {
    private static final DynamicCommandExceptionType INAPPLICABLE_OPTION = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.inapplicable", value)));
    private static final DynamicCommandExceptionType UNKNOWN_OPTION = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.unknown", value)));
    private static final DynamicCommandExceptionType UNKNOWN_GAME_MODE = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.gamemode.invalid", value)));

    private final StringReader reader;
    private final String key;

    private static final Map<String, Option> OPTIONS = new HashMap<>();

    public EntitySelectorOptions(StringReader reader, String key) {
        this.reader = reader;
        this.key = key;
    }

    public static void register(String key, Modifier modifier, Predicate<EntitySelectorParser> canUse, Text description) {
        OPTIONS.put(key, new Option(modifier, canUse, description));
    }

    static {
        register("gamemode", (parser) -> {
            parser.setSuggestions((builder, consumer) -> {
                String string = builder.getRemaining().toLowerCase(Locale.ROOT);
                boolean bl = !parser.hasGamemodeNotEquals();
                boolean bl2 = true;
                if (!string.isEmpty()) {
                    if (string.charAt(0) == '!') {
                        bl = false;
                        string = string.substring(1);
                    } else {
                        bl2 = false;
                    }
                }
                for (Gamemode gameMode : Gamemode.gamemodesList) {
                    if (!CommanderHelper.getStringToSuggest(gameMode.languageKey, string).isPresent()) continue;
                    String stringToSuggest = CommanderHelper.getStringToSuggest(gameMode.languageKey, string).get();
                    if (bl2) {
                        builder.suggest("!" + stringToSuggest);
                    }
                    if (!bl) continue;
                    builder.suggest(stringToSuggest);
                }
                return builder.buildFuture();
            });
            int cursor = parser.getReader().getCursor();
            boolean invert = parser.shouldInvertValue();
            String value = parser.getReader().readUnquotedString();

            Gamemode gamemode = null;
            for (Gamemode iteratedGameMode : Gamemode.gamemodesList) {
                if (CommanderHelper.matchesKeyString(iteratedGameMode.languageKey, value)) {
                    gamemode = iteratedGameMode;
                }
            }
            if (gamemode == null) {
                parser.getReader().setCursor(cursor);
                throw UNKNOWN_GAME_MODE.createWithContext(parser.getReader(), value);
            }

            parser.addPredicate((entity) -> {
                if (!(entity instanceof EntityPlayer)) return false;
                return CommanderHelper.matchesKeyString(((EntityPlayer) entity).gamemode.languageKey, value) != invert;
            });

            if (invert) {
                parser.setHasGamemodeNotEquals(true);
            } else {
                parser.setHasGamemodeEquals(true);
            }
        }, parser -> !parser.hasGamemodeEquals(), new TextTranslatable("argument_types.commander.entity.selector.options.gamemode.description"));
    }

    public static Modifier get(EntitySelectorParser entitySelectorParser, String string, int i) throws CommandSyntaxException {
        Option option = OPTIONS.get(string);
        if (option != null) {
            if (option.canUse.test(entitySelectorParser)) {
                return option.modifier;
            }
            throw INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), string);
        }
        entitySelectorParser.getReader().setCursor(i);
        throw UNKNOWN_OPTION.createWithContext(entitySelectorParser.getReader(), string);
    }

    public static void suggestNames(EntitySelectorParser entitySelectorParser, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Option> entry : OPTIONS.entrySet()) {
            if (!entry.getValue().canUse.test(entitySelectorParser) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) continue;
            suggestionsBuilder.suggest(entry.getKey() + "=", () -> entry.getValue().description.toString());
        }
    }

    static class Option {
        final Modifier modifier;
        final Predicate<EntitySelectorParser> canUse;
        final Text description;

        Option(Modifier modifier, Predicate<EntitySelectorParser> canUse, Text description) {
            this.modifier = modifier;
            this.canUse = canUse;
            this.description = description;
        }
    }

    @FunctionalInterface
    interface Modifier {
        void handle(EntitySelectorParser parser) throws CommandSyntaxException;
    }
}
