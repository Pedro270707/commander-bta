package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.world.weather.Weather;
import net.pedroricardo.commander.CommanderHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WeatherArgumentType implements ArgumentType<Weather> {
    private static final Collection<String> EXAMPLES = Arrays.asList(Weather.overworldClear.languageKey, Weather.overworldFog.languageKey);

    public static ArgumentType<Weather> weather() {
        return new WeatherArgumentType();
    }

    @Override
    public Weather parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Weather weather : Weather.weatherList) {
            if (weather == null) continue;
            if (CommanderHelper.matchesKeyString(weather.languageKey, string)) {
                return weather;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.weather.invalid_weather"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Weather weather : Weather.weatherList) {
            if (weather == null) continue;
            Optional<String> optional = CommanderHelper.getStringToSuggest(weather.languageKey, remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
