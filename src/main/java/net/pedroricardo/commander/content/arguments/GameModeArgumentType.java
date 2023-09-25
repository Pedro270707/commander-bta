package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.gamemode.Gamemode;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GameModeArgumentType implements ArgumentType<Gamemode> {
    private static final Collection<String> EXAMPLES = Arrays.asList(Gamemode.creative.languageKey, Gamemode.survival.languageKey);

    public static ArgumentType<Gamemode> gameMode() {
        return new GameModeArgumentType();
    }

    @Override
    public Gamemode parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Gamemode gamemode : Gamemode.gamemodesList) {
            if (CommanderHelper.matchesKeyString(gamemode.languageKey, string)) {
                return gamemode;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.game_mode.invalid_game_mode"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Gamemode gamemode : Gamemode.gamemodesList) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(gamemode.languageKey, remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
