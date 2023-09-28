package net.pedroricardo.commander.content.arguments;

import com.b100.utils.Utils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.achievement.AchievementList;
import net.minecraft.core.block.Block;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.commands.GenerateCommand;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.util.helper.ReflectionHelper;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.mixin.StatNameAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WorldFeatureArgumentType implements ArgumentType<WorldFeature> {
    private static final Collection<String> EXAMPLES = Arrays.asList("achievement.acquireIron", "acquireIron");

    public static ArgumentType<WorldFeature> worldFeature() {
        return new WorldFeatureArgumentType();
    }

    @Override
    public WorldFeature parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();
        Class<? extends WorldFeature> worldFeatureClass = null;

        for (Map.Entry<String, Class<? extends WorldFeature>> entry : CommanderHelper.WORLD_FEATURES.entrySet()) {
            if (CommanderHelper.matchesKeyString(entry.getKey(), string)) {
                worldFeatureClass = entry.getValue();
            }
        }

        if (worldFeatureClass == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);

        boolean hasParameters = false;
        Constructor<?> constructor = null;

        for (Constructor<?> c : worldFeatureClass.getConstructors()) {
            constructor = c;
            if (c.getParameters().length != 0) hasParameters = true;
        }

        if (!hasParameters && constructor != null) {
            try {
                return (WorldFeature) constructor.newInstance();
            } catch (Exception e) {
                throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.world_feature.invalid_world_feature"));
            }
        }

        if (reader.canRead() && reader.peek() == '[') {
            List<Object> parameters = new ArrayList<>();
            constructor = null;

            int cursor = reader.getCursor();

            for (Constructor<?> c : worldFeatureClass.getConstructors()) {
                reader.setCursor(cursor);
                for (Parameter parameter : c.getParameters()) {
                    reader.skip();
                    reader.skipWhitespace();
                    int cursor2 = reader.getCursor();
                    if (parameter.getType() == Integer.TYPE) {
                        parameters.add(reader.readInt());
                    } else if (parameter.getType() == Block.class) {
                        parameters.add(parseBlock(reader));
                    } else if (parameter.getType() == String.class) {
                        parameters.add(reader.readString());
                    } else {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader);
                    }
                    reader.skipWhitespace();
                    if (!reader.canRead() || (reader.peek() != ',' && reader.peek() != ']')) {
                        reader.setCursor(cursor2);
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
                    }
                }
                constructor = c;
            }
            if (constructor != null && reader.canRead() && reader.peek() == ']') {
                reader.skip();
                try {
                    return (WorldFeature) constructor.newInstance(Utils.toArray(Object.class, parameters));
                } catch (Exception e) {
                    Commander.LOGGER.info(e.toString());
                    throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.world_feature.invalid_world_feature"));
                }
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.world_feature.invalid_world_feature"));
    }

    private static Block parseBlock(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Block blockInList : Block.blocksList) {
            if (blockInList == null) continue;
            if (CommanderHelper.matchesKeyString(blockInList.getKey(), string)) {
                return blockInList;
            }
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_block"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Map.Entry<String, Class<? extends WorldFeature>> entry : CommanderHelper.WORLD_FEATURES.entrySet()) {
            Optional<String> optional = CommanderHelper.getStringToSuggest(entry.getKey(), remaining);
            optional.ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
