package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.biome.Biome;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.StringReader.isQuotedStringStart;

public class BiomeArgumentType implements ArgumentType<Biome> {
    private static final Collection<String> EXAMPLES = Arrays.asList("overworld", "nether");

    public static BiomeArgumentType biome() {
        return new BiomeArgumentType();
    }

    @Override
    public Biome parse(StringReader reader) throws CommandSyntaxException {
        final String string = readResourceLocation(reader);

        for (Biome biome : Registries.BIOMES) {
            if (CommanderHelper.matchesKeyString(Registries.BIOMES.getKey(biome), string)) {
                return biome;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.biome.invalid_biome"));
    }

    public String readResourceLocation(StringReader reader) {
        if (!reader.canRead()) {
            return "";
        }
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    public static boolean isAllowedInResourceLocation(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Biome biome : Registries.BIOMES) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(Registries.BIOMES.getKey(biome), remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}