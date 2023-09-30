package net.pedroricardo.commander.content.helpers;

import com.b100.utils.Utils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.CommanderHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class WorldFeatureParser {
    private final SimpleCommandExceptionType NO_PARAMETERS = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.world_feature.no_parameters"));

    private final StringReader reader;
    private int startPosition = 0;
    
    private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = CommanderHelper.NO_SUGGESTIONS;
    
    public WorldFeatureParser(StringReader reader) {
        this.reader = reader;
    }

    private CompletableFuture<Suggestions> suggestWorldFeatures(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(this.startPosition);
        consumer.accept(suggestionsBuilder2);
        return suggestionsBuilder.add(suggestionsBuilder2).buildFuture();
    }

    public WorldFeature parse() throws CommandSyntaxException {
        this.startPosition = this.reader.getCursor();
        this.suggestions = this::suggestWorldFeatures;
        final String string = this.reader.readString();
        Class<? extends WorldFeature> worldFeatureClass = null;

        for (Map.Entry<String, Class<? extends WorldFeature>> entry : CommanderHelper.WORLD_FEATURES.entrySet()) {
            if (CommanderHelper.matchesKeyString(entry.getKey(), string)) {
                worldFeatureClass = entry.getValue();
            }
        }

        if (worldFeatureClass == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);

        boolean hasParameters = false;
        Constructor<?> constructor = null;

        for (Constructor<?> c : worldFeatureClass.getConstructors()) {
            constructor = c;
            hasParameters = c.getParameters().length != 0;
        }

        if (!hasParameters && constructor != null) {
            if (this.reader.canRead() && this.reader.peek() == '[') {
                throw NO_PARAMETERS.createWithContext(this.reader);
            }
            try {
                return (WorldFeature) constructor.newInstance();
            } catch (Exception e) {
                throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.world_feature.invalid_world_feature"));
            }
        }

        return this.parseParameters(worldFeatureClass);
    }

    private WorldFeature parseParameters(Class<? extends WorldFeature> worldFeatureClass) throws CommandSyntaxException {
        this.suggestions = this::suggestOpenParameters;
        Constructor<?> constructor;
        if (this.reader.canRead()) this.suggestions = CommanderHelper.NO_SUGGESTIONS;
        if (this.reader.canRead() && this.reader.peek() == '[') {
            List<Object> parameters = new ArrayList<>();
            this.reader.skip();

            Constructor<?> c = getConstructorWithMostParameters(worldFeatureClass.getConstructors());

            if (c == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);

            for (Parameter parameter : c.getParameters()) {
                this.suggestions = CommanderHelper.NO_SUGGESTIONS;
                this.reader.skipWhitespace();
                Object parsedParameter = this.parseParameter(parameter);
                parameters.add(parsedParameter);

                boolean lastParameter = parameters.size() == c.getParameters().length;

                this.reader.skipWhitespace();
                if (!this.reader.canRead()) {
                    this.suggestions = lastParameter ? this::suggestParametersClose : this::suggestParametersNext;
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);
                }
                if ((lastParameter && this.reader.peek() == ']') || (!lastParameter && this.reader.peek() == ','))
                    this.reader.skip();
                else throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);
            }

            constructor = c;
            this.suggestions = CommanderHelper.NO_SUGGESTIONS;

            try {
                return (WorldFeature) constructor.newInstance(Utils.toArray(Object.class, parameters));
            } catch (Exception e) {
                throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.world_feature.invalid_world_feature"));
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.world_feature.invalid_world_feature"));
    }

    private Object parseParameter(Parameter parameter) throws CommandSyntaxException {
        this.suggestions = CommanderHelper.NO_SUGGESTIONS;
        return WorldFeatureParameterTypes.get(parameter.getType(), this.reader, this);
    }

    private CompletableFuture<Suggestions> suggestOpenParameters(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        suggestionsBuilder.suggest(String.valueOf('['));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestParametersNext(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        suggestionsBuilder.suggest(String.valueOf(','));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestParametersClose(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        suggestionsBuilder.suggest(String.valueOf(']'));
        return suggestionsBuilder.buildFuture();
    }

    public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> biFunction) {
        this.suggestions = biFunction;
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }

    private static Constructor<?> getConstructorWithMostParameters(Constructor<?>[] constructors) {
        if (constructors.length == 0) return null;
        Constructor<?> constructor = constructors[0];
        for (Constructor<?> c : constructors) {
            if (c.getParameters().length <= constructor.getParameters().length) continue;
            constructor = c;
        }
        return constructor;
    }
}
