package net.pedroricardo.commander.content.arguments;

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
    public static ArgumentType<Achievement> achievement() {
        return new AchievementArgumentType();
    }

    @Override
    public Achievement parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Achievement achievement : AchievementList.achievementList) {
            if (((StatNameAccessor)achievement).statName().equals(string)) {
                reader.skip();
                return achievement;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.achievement.invalid_achievement"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (Achievement achievement : AchievementList.achievementList) {
            if (((StatNameAccessor)achievement).statName().startsWith(builder.getRemaining())) {
                builder.suggest(((StatNameAccessor)achievement).statName(), achievement::getStatName);
            }
        }
        return builder.buildFuture();
    }

    public static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-' || c >= 'A' && c <= 'Z';
    }
}
