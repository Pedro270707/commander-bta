package net.pedroricardo.commander;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.util.helper.LogPrintStream;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CommanderHelper {
    public static final Map<String, Class<? extends WorldFeature>> WORLD_FEATURES = new HashMap<>();

    public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> NO_SUGGESTIONS = (builder, consumer) -> builder.buildFuture();
    public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_BLOCKS = (builder, consumer) -> {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Block block : Block.blocksList) {
            if (block == null) continue;
            CommanderHelper.getStringToSuggest(block.getKey(), remaining).ifPresent(builder::suggest);
        }
        return builder.buildFuture();
    };

    public static CompletableFuture<Suggestions> suggest(String string, SuggestionsBuilder suggestionsBuilder) {
        String stringRemaining = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        if (matchesSubStr(stringRemaining, string.toLowerCase(Locale.ROOT))) suggestionsBuilder.suggest(string);
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggest(Iterable<String> iterable, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string2 : iterable) {
            if (!matchesSubStr(string, string2.toLowerCase(Locale.ROOT))) continue;
            suggestionsBuilder.suggest(string2);
        }
        return suggestionsBuilder.buildFuture();
    }

    public static boolean matchesSubStr(String string, String string2) {
        int i = 0;
        while (!string2.startsWith(string, i)) {
            if ((i = string2.indexOf(95, i)) < 0) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static Optional<String> getStringToSuggest(String checkedString, String input) {
        if (checkedString.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))) {
            return Optional.of(checkedString);
        } else {
            if (checkedString.contains(".") && checkedString.toLowerCase(Locale.ROOT).substring(checkedString.indexOf('.') + 1).startsWith(input.toLowerCase(Locale.ROOT))) {
                return Optional.of(checkedString.substring(checkedString.indexOf('.') + 1));
            }
        }
        return Optional.empty();
    }

    public static boolean matchesKeyString(String checkedString, String input) {
        if (checkedString.equals(input)) {
            return true;
        }
        return checkedString.substring(checkedString.indexOf('.') + 1).equals(input);
    }

    public static String getEntityName(Entity entity) {
        if (entity instanceof EntityLiving) {
            if (!LogPrintStream.removeColorCodes(((EntityLiving) entity).getDisplayName()).isEmpty()) {
                return LogPrintStream.removeColorCodes(((EntityLiving) entity).getDisplayName());
            }
        }
        return EntityDispatcher.getEntityString(entity);
    }

    public static JsonObject getDefaultServerSuggestions() {
        JsonObject serverSuggestions = new JsonObject();
        serverSuggestions.add("suggestions", new JsonArray());
        serverSuggestions.add("usage", new JsonArray());
        JsonArray exceptions = new JsonArray();
        JsonObject exception = new JsonObject();
        exception.addProperty("value", "This server does not have Commander installed");
        exceptions.add(exception);
        serverSuggestions.add("exceptions", exceptions);
        serverSuggestions.add("last_child", new JsonObject());
        return serverSuggestions;
    }

    public static int getVolume(CommanderCommandSource source, IntegerCoordinates first, IntegerCoordinates second) throws CommandSyntaxException {
        return (int) (MathHelper.abs(first.getX(source) - second.getX(source)) * MathHelper.abs(first.getY(source, true) - second.getY(source, true)) * MathHelper.abs(first.getZ(source) - second.getZ(source)));
    }

    public static void init() {
        try {
            for (Class<?> clazz : CommanderReflectionHelper.getAllClasses(className -> className.startsWith("net.minecraft.core.world.generate.feature"))) {
                if (!WorldFeature.class.isAssignableFrom(clazz)) continue;
                WORLD_FEATURES.put(clazz.getSimpleName().substring(12), (Class<? extends WorldFeature>) clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
