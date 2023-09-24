package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.text.Text;
import net.minecraft.core.lang.text.TextTranslatable;
import net.minecraft.core.player.gamemode.Gamemode;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.CommanderHelper;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class EntitySelectorOptions {
    private static final DynamicCommandExceptionType INAPPLICABLE_OPTION = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.inapplicable", value)));
    private static final DynamicCommandExceptionType UNKNOWN_OPTION = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.unknown", value)));
    private static final DynamicCommandExceptionType UNKNOWN_GAME_MODE = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.gamemode.invalid", value)));
    private static final DynamicCommandExceptionType UNKNOWN_SORT = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.sort.invalid", value)));
    private static final DynamicCommandExceptionType UNKNOWN_ENTITY_TYPE = new DynamicCommandExceptionType(value -> (() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.entity.selector.options.type.invalid", value)));
    private static final SimpleCommandExceptionType NEGATIVE_DISTANCE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.options.distance.invalid"));
    private static final SimpleCommandExceptionType LIMIT_TOO_SMALL = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.options.limit.invalid"));

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
        EntitySelectorOptions.register("name", entitySelectorParser -> {
                    int i = entitySelectorParser.getReader().getCursor();
                    boolean bl = entitySelectorParser.shouldInvertValue();
                    String string = entitySelectorParser.getReader().readString();
                    if (entitySelectorParser.hasNameNotEquals() && !bl) {
                        entitySelectorParser.getReader().setCursor(i);
                        throw INAPPLICABLE_OPTION.createWithContext(entitySelectorParser.getReader(), "name");
                    }
                    if (bl) {
                        entitySelectorParser.setHasNameNotEquals(true);
                    } else {
                        entitySelectorParser.setHasNameEquals(true);
                    }
                    entitySelectorParser.addPredicate(entity -> {
                        if (!(entity instanceof EntityLiving)) return bl;
                        else if (entity instanceof EntityPlayer) return ((EntityPlayer)entity).username.equals(string) != bl;
                        else if (!((EntityLiving)entity).getDisplayName().startsWith("§") || ((EntityLiving)entity).getDisplayName().length() < 2) return ((EntityLiving)entity).getDisplayName().equals(string) != bl;
                        return ((EntityLiving)entity).getDisplayName().substring(2).equals(string) != bl;
                    });
                }, entitySelectorParser -> !entitySelectorParser.hasNameEquals(), new TextTranslatable("argument_types.commander.entity.selector.options.name.description"));
        register("distance", (parser) -> {
            int cursor = parser.getReader().getCursor();
            MinMaxBounds.Doubles bounds = MinMaxBounds.Doubles.fromReader(parser.getReader());
            if ((bounds.getMin() != null && bounds.getMin() < 0) || (bounds.getMax() != null && bounds.getMax() < 0)) {
                parser.getReader().setCursor(cursor);
                throw NEGATIVE_DISTANCE.createWithContext(parser.getReader());
            }
            parser.setDistance(bounds);
        }, parser -> parser.getDistance().isAny(), new TextTranslatable("argument_types.commander.entity.selector.options.distance.description"));
        register("type", (parser) -> {
            int cursor = parser.getReader().getCursor();
            boolean invert = parser.shouldInvertValue();

            parser.setSuggestions((builder, consumer) -> {
                String string = builder.getRemaining().toLowerCase(Locale.ROOT);
                if (!string.isEmpty()) {
                    if (string.charAt(0) == '!') {
                        string = string.substring(1);
                    }
                }
                CommanderHelper.suggest("!Player", builder);
                CommanderHelper.suggest("Player", builder);
                for (String key : EntityDispatcher.stringToClassMapping.keySet()) {
                    if (!key.toLowerCase(Locale.ROOT).startsWith(string)) continue;
                    CommanderHelper.suggest("!" + key, builder);
                    if (invert) continue;
                    CommanderHelper.suggest(key, builder);
                }
                return builder.buildFuture();
            });

            if (parser.isTypeInverse() && !invert) {
                parser.getReader().setCursor(cursor);
                throw INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "type");
            }
            if (invert) {
                parser.setTypeInverse(true);
            }
            String type = parser.getReader().readUnquotedString();
            if (type.equals("Player")) {
                parser.setLimitToType(EntityPlayer.class);
            } else {
                if (EntityDispatcher.stringToClassMapping.containsKey(type)) {
                    parser.setLimitToType(EntityDispatcher.stringToClassMapping.get(type));
                } else {
                    parser.getReader().setCursor(cursor);
                    throw UNKNOWN_ENTITY_TYPE.createWithContext(parser.getReader(), type);
                }
            }
        }, parser -> !parser.hasType(), new TextTranslatable("argument_types.commander.entity.selector.options.type.description"));
        register("limit", (parser) -> {
            int cursor = parser.getReader().getCursor();
            int limit = parser.getReader().readInt();
            if (limit < 1) {
                parser.getReader().setCursor(cursor);
                throw LIMIT_TOO_SMALL.createWithContext(parser.getReader());
            }
            parser.setMaxResults(limit);
            parser.setHasLimit(true);
        }, parser -> !parser.hasLimit(), new TextTranslatable("argument_types.commander.entity.selector.options.limit.description"));
        register("sort", (parser) -> {
            int i = parser.getReader().getCursor();
            String string = parser.getReader().readUnquotedString();
            parser.setSuggestions((suggestionsBuilder, consumer) -> CommanderHelper.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), suggestionsBuilder));
            BiConsumer<Entity, List<? extends Entity>> sort;
            switch (string) {
                case "nearest":
                    sort = EntitySelectorParser.ORDER_NEAREST;
                    break;
                case "furthest":
                    sort = EntitySelectorParser.ORDER_FURTHEST;
                    break;
                case "random":
                    sort = EntitySelectorParser.ORDER_RANDOM;
                    break;
                case "arbitrary":
                    sort = EntitySelectorParser.ORDER_ARBITRARY;
                    break;
                default:
                    parser.getReader().setCursor(i);
                    throw UNKNOWN_SORT.createWithContext(parser.getReader(), string);
            }
            parser.setOrder(sort);
            parser.setSorted(true);
        }, parser -> !parser.isSorted(), new TextTranslatable("argument_types.commander.entity.selector.options.sort.description"));
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

            parser.setIncludesEntities(false);
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
