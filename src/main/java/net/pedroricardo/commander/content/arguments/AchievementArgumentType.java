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
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.mixin.StatNameAccessor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AchievementArgumentType implements ArgumentType<Achievement> {
    private static final Collection<String> EXAMPLES = Arrays.asList("achievement.acquireIron", "acquireIron");

    public static ArgumentType<Achievement> achievement() {
        return new AchievementArgumentType();
    }

    @Override
    public Achievement parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Achievement achievement : AchievementList.achievementList) {
            if (CommanderHelper.matchesKeyString(((StatNameAccessor)achievement).statName(), string)) {
                return achievement;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.achievement.invalid_achievement"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Achievement achievement : AchievementList.achievementList) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(((StatNameAccessor)achievement).statName(), remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
