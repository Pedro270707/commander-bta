package net.pedroricardo.commander.content.arguments;

import com.b100.utils.Utils;
import com.google.common.collect.Iterables;
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
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.helpers.EntitySelectorParser;
import net.pedroricardo.commander.content.helpers.WorldFeatureParser;
import net.pedroricardo.commander.mixin.StatNameAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WorldFeatureArgumentType implements ArgumentType<WorldFeature> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Dungeon[2, 2, 2]", "Cactus");

    public static ArgumentType<WorldFeature> worldFeature() {
        return new WorldFeatureArgumentType();
    }

    @Override
    public WorldFeature parse(StringReader reader) throws CommandSyntaxException {
        WorldFeatureParser parser = new WorldFeatureParser(reader);
        return parser.parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        WorldFeatureParser worldFeatureParser = new WorldFeatureParser(stringReader);
        try {
            worldFeatureParser.parse();
        } catch (CommandSyntaxException ignored) {}
        return worldFeatureParser.fillSuggestions(builder, suggestionsBuilder ->
                CommanderHelper.suggest(CommanderHelper.WORLD_FEATURES.keySet(), suggestionsBuilder));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
