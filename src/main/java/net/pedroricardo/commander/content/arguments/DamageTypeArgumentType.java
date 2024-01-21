package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.DamageType;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DamageTypeArgumentType implements ArgumentType<DamageType> {
    private static final Collection<String> EXAMPLES = Arrays.asList("damagetype.combat", "damagetype.blast");

    public static DamageTypeArgumentType damageType() {
        return new DamageTypeArgumentType();
    }

    @Override
    public DamageType parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (DamageType damageType : DamageType.values()) {
            if (CommanderHelper.matchesKeyString(damageType.getLanguageKey(), string)) {
                return damageType;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.damage_type.invalid_damage_type"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (DamageType damageType : DamageType.values()) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(damageType.getLanguageKey(), remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
