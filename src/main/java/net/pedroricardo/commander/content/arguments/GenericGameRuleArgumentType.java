package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.data.gamerule.GameRule;
import net.minecraft.core.lang.I18n;

import java.util.concurrent.CompletableFuture;

public class GenericGameRuleArgumentType implements ArgumentType<Object> {
    private final GameRule<?> gameRule;
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.gamerule.invalid_value"));

    private GenericGameRuleArgumentType(GameRule<?> gameRule) {
        this.gameRule = gameRule;
    }

    public static GenericGameRuleArgumentType gameRule(GameRule<?> gameRule) {
        return new GenericGameRuleArgumentType(gameRule);
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        StringBuilder read = new StringBuilder();
        Object value = null;
        while (reader.canRead() && (StringReader.isQuotedStringStart(reader.peek()) || StringReader.isAllowedInUnquotedString(reader.peek())) && value == null) {
            read.append(reader.readString());
            value = this.gameRule.parseFromString(read.toString());
        }

        if (value != null) return value;
        throw FAILURE.create();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }
}
