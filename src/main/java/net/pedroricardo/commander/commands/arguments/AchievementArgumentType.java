package net.pedroricardo.commander.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.achievement.AchievementList;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.mixin.StatNameAccessor;

import java.util.concurrent.CompletableFuture;

public class AchievementArgumentType implements ArgumentType<Achievement> {
    public static ArgumentType<Achievement> achievementParameter() {
        return new AchievementArgumentType();
    }

    @Override
    public Achievement parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while(reader.canRead() && isCharValid(reader.peek())) {
            reader.skip();
        }

        final String string = reader.getString().substring(i, reader.getCursor());

        for (Achievement achievement : AchievementList.achievementList) {
            if (((StatNameAccessor)achievement).statName().equals(string)) {
                return achievement;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKeyAndFormat("commands.commander.achievement.invalid_achievement", string));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (Achievement achievement : AchievementList.achievementList) {
            if (((StatNameAccessor)achievement).statName().startsWith(builder.getRemaining())) {
                builder.suggest(((StatNameAccessor)achievement).statName());
            }
        }
        return builder.buildFuture();
    }

    public static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-' || c >= 'A' && c <= 'Z';
    }
}
