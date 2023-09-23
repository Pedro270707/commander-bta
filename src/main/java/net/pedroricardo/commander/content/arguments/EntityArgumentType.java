package net.pedroricardo.commander.content.arguments;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;
import net.pedroricardo.commander.content.helpers.EntitySelectorParser;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class EntityArgumentType implements ArgumentType<EntitySelector> {
    private final boolean singleEntity, playerOnly;

    private EntityArgumentType(boolean singleEntity, boolean playerOnly) {
        this.singleEntity = singleEntity;
        this.playerOnly = playerOnly;
    }

    public static EntityArgumentType entities() {
        return new EntityArgumentType(false, false);
    }

    public static EntityArgumentType entity() {
        return new EntityArgumentType(true, false);
    }

    public static EntityArgumentType players() {
        return new EntityArgumentType(false, true);
    }

    public static EntityArgumentType player() {
        return new EntityArgumentType(true, true);
    }

    @Override
    public EntitySelector parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        EntitySelectorParser entitySelectorParser = new EntitySelectorParser(reader);
        EntitySelector entitySelector = entitySelectorParser.parse();
        if (this.singleEntity && entitySelector.getMaxResults() > 1) {
            reader.setCursor(cursor);
            if (this.playerOnly) {
                throw CommanderExceptions.singlePlayerOnly().createWithContext(reader);
            }
            throw CommanderExceptions.singleEntityOnly().createWithContext(reader);
        }
        if (this.playerOnly && entitySelector.includesEntities() && !entitySelector.isCurrentEntity()) {
            reader.setCursor(cursor);
            throw CommanderExceptions.playerOnly().createWithContext(reader);
        }
        return entitySelector;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S s = context.getSource();
        if (s instanceof CommanderCommandSource) {
            CommanderCommandSource source = (CommanderCommandSource)s;
            StringReader stringReader = new StringReader(builder.getInput());
            stringReader.setCursor(builder.getStart());
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader, source.hasAdmin());
            try {
                entitySelectorParser.parse();
            } catch (CommandSyntaxException ignored) {}
            return entitySelectorParser.fillSuggestions(builder, suggestionsBuilder -> {
                Collection<String> collection = source.getPlayerNames();
                Iterable<String> iterable = this.playerOnly ? collection : Iterables.concat(collection, source.getEntitySuggestions());
                CommanderHelper.suggest(iterable, suggestionsBuilder);
            });
        }
        return Suggestions.empty();
    }
}
