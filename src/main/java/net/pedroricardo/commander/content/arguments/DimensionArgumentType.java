package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.Dimension;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DimensionArgumentType implements ArgumentType<Dimension> {
    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "nether");

    public static DimensionArgumentType dimension() {
        return new DimensionArgumentType();
    }

    @Override
    public Dimension parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Map.Entry<Integer, Dimension> dimension : Dimension.getDimensionList().entrySet()) {
            if (CommanderHelper.matchesKeyString(dimension.getValue().languageKey, string) || string.equals(String.valueOf(dimension.getKey()))) {
                return dimension.getValue();
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.dimension.invalid_dimension"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Dimension dimension : Dimension.getDimensionList().values()) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(dimension.languageKey, remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}