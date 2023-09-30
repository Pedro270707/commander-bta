package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.block.Block;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorldFeatureParameterTypes {
    private static final Map<Class<?>, ReadFunction> FEATURE_MAP = new HashMap<>();

    static {
        FEATURE_MAP.put(Integer.TYPE, (reader, parser) -> reader.readInt());
        FEATURE_MAP.put(Block.class, WorldFeatureParameterTypes::parseBlock);
        FEATURE_MAP.put(String.class, (reader, parser) -> reader.readString());
    }

    public static void register(Class<?> clazz, ReadFunction function) {
        FEATURE_MAP.put(clazz, function);
    }

    public static Map<Class<?>, ReadFunction> getFeatures() {
        return new HashMap<>(FEATURE_MAP);
    }

    public static Object get(Class<?> clazz, StringReader stringReader, WorldFeatureParser parser) throws CommandSyntaxException {
        if (FEATURE_MAP.containsKey(clazz)) return FEATURE_MAP.get(clazz).handle(stringReader, parser);
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(stringReader);
    }

    private static Block parseBlock(StringReader reader, WorldFeatureParser parser) throws CommandSyntaxException {
        parser.setSuggestions((builder, consumer) -> {
            String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            for (Block block : Block.blocksList) {
                if (block == null) continue;
                CommanderHelper.getStringToSuggest(block.getKey(), remaining).ifPresent(builder::suggest);
            }
            return builder.buildFuture();
        });

        int cursor = reader.getCursor();
        final String string = reader.readString();

        for (Block blockInList : Block.blocksList) {
            if (blockInList == null) continue;
            if (CommanderHelper.matchesKeyString(blockInList.getKey(), string)) {
                return blockInList;
            }
        }
        reader.setCursor(cursor);
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_block"));
    }

    public interface ReadFunction {
        Object handle(StringReader reader, WorldFeatureParser parser) throws CommandSyntaxException;
    }
}
