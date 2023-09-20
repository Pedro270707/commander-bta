package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.lang.I18n;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntitySummonArgumentType implements ArgumentType<Class<? extends Entity>> {
    private static final List<String> EXAMPLES = Arrays.asList("Creeper", "Skeleton", "Slime");

    public static ArgumentType<Class<? extends Entity>> entity() {
        return new EntitySummonArgumentType();
    }

    @Override
    public Class<? extends Entity> parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (String entityName : EntityDispatcher.stringToClassMapping.keySet()) {
            if (entityName.equals(string)) {
                return EntityDispatcher.stringToClassMapping.get(entityName);
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.entity_summon.invalid_entity"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String entityName : EntityDispatcher.stringToClassMapping.keySet()) {
            if (entityName.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(entityName);
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
