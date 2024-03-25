package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.sound.SoundTypes;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SoundCategoryArgumentType implements ArgumentType<SoundCategory> {
    private static final Collection<String> EXAMPLES = Arrays.asList("mob.pig", "mob.zombiepig.zpighurt", "ambient.cave.cave");

    public static ArgumentType<SoundCategory> soundCategory() {
        return new SoundCategoryArgumentType();
    }

    @Override
    public SoundCategory parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (SoundCategory soundCategory : SoundCategory.values()) {
            if (CommanderHelper.matchesKeyString(soundCategory.name(), string)) {
                return soundCategory;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.sound_category.invalid_sound_category"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (SoundCategory soundCategory : SoundCategory.values()) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(soundCategory.name(), remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
