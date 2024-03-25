package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundTypes;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SoundTypeArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("mob.pig", "mob.zombiepig.zpighurt", "ambient.cave.cave");

    public static ArgumentType<String> soundType() {
        return new SoundTypeArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        if (SoundTypes.getSoundIds().isEmpty()) SoundTypes.registerSounds();
        for (String sound : SoundTypes.getSoundIds().keySet()) {
            if (CommanderHelper.matchesKeyString(sound, string)) {
                return sound;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.sound_type.invalid_sound_type"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        if (SoundTypes.getSoundIds().isEmpty()) SoundTypes.registerSounds();
        for (String sound : SoundTypes.getSoundIds().keySet()) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(sound, remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
