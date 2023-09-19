package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;
import net.pedroricardo.commander.content.helpers.EntitySelectorParser;

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
        if ("@p".startsWith(builder.getRemainingLowerCase()))
            builder.suggest("@p", () -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.nearest_player"));
        if ("@a".startsWith(builder.getRemainingLowerCase()))
            builder.suggest("@a", () -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.all_players"));
        if ("@r".startsWith(builder.getRemainingLowerCase()))
            builder.suggest("@r", () -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.random_player"));
        if ("@s".startsWith(builder.getRemainingLowerCase()))
            builder.suggest("@s", () -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.self"));
        if ("@e".startsWith(builder.getRemainingLowerCase()))
            builder.suggest("@e", () -> I18n.getInstance().translateKey("argument_types.commander.entity.selector.all_entities"));
        for (String name : ((CommanderCommandSource)context.getSource()).getEntitySuggestions()) {
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}
