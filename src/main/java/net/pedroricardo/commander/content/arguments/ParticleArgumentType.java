package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.sound.SoundCategory;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ParticleArgumentType implements ArgumentType<String> {
    private static final List<String> EXAMPLES = Arrays.asList("smoke", "bubble", "explode");
    private static final List<String> SUGGESTIONS = Arrays.asList(
            "bubble",
            "smoke",
            "note",
            "portal",
            "explode",
            "flame",
            "blueflame",
            "soulflame",
            "lava",
            "footstep",
            "splash",
            "largesmoke",
            "reddust",
            "item",
            "block",
            "snowshovel",
            "heart",
            "slimechunk",
            "fireflyGreen",
            "fireflyBlue",
            "fireflyOrange",
            "fireflyRed",
            "arrowtrail",
            "fallingleaf",
            "boatbreak"
    );

    public static ArgumentType<String> particle() {
        return new ParticleArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (String string : SUGGESTIONS) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(string, remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }
}
