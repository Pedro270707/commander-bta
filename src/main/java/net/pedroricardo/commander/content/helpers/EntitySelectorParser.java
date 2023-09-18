package net.pedroricardo.commander.content.helpers;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class EntitySelectorParser {
    private final BiConsumer<Entity, List<? extends Entity>> ORDER_RANDOM = (sender, entities) -> Collections.shuffle(entities);
    private final BiConsumer<Entity, List<? extends Entity>> ORDER_ARBITRARY = (sender, entities) -> {};
    private final BiConsumer<Entity, List<? extends Entity>> ORDER_CLOSEST = (sender, entities) -> entities.sort((firstEntity, secondEntity) -> Doubles.compare(firstEntity.distanceTo(sender), secondEntity.distanceTo(sender)));
    private final BiConsumer<Entity, List<? extends Entity>> ORDER_FARTHEST = (sender, entities) -> entities.sort((firstEntity, secondEntity) -> Doubles.compare(secondEntity.distanceTo(sender), firstEntity.distanceTo(sender)));

    private final StringReader reader;

    private int maxResults;
    private boolean includesEntities;
    private BiConsumer<Entity, List<? extends Entity>> order;
    private @Nullable Class<? extends Entity> limitToType;
    private boolean currentEntity;

    public EntitySelectorParser(StringReader reader) {
        this.reader = reader;
    }

    private void parseSelector() throws CommandSyntaxException {
        if (!this.reader.canRead()) throw CommanderExceptions.invalidSelector().createWithContext(this.reader);

        switch (this.reader.peek()) {
            case 'p':
                this.maxResults = 1;
                this.includesEntities = false;
                this.order = ORDER_CLOSEST;
                this.limitToType = EntityPlayer.class;
                this.currentEntity = false;
                break;
            case 'a':
                this.maxResults = Integer.MAX_VALUE;
                this.includesEntities = false;
                this.order = ORDER_ARBITRARY;
                this.limitToType = EntityPlayer.class;
                this.currentEntity = false;
                break;
            case 'r':
                this.maxResults = 1;
                this.includesEntities = false;
                this.order = ORDER_RANDOM;
                this.limitToType = EntityPlayer.class;
                this.currentEntity = false;
                break;
            case 's':
                this.maxResults = 1;
                this.includesEntities = true;
                this.order = ORDER_ARBITRARY;
                this.limitToType = EntityPlayer.class;
                this.currentEntity = true;
                break;
            case 'e':
                this.maxResults = Integer.MAX_VALUE;
                this.includesEntities = true;
                this.order = ORDER_ARBITRARY;
                this.currentEntity = false;
                break;
        }
        reader.skip();
        // Temporary; replace later with selectors (e.g. @e[type=Sheep,name="the sheep"])
        if (reader.canRead() && reader.peek() != ' ') {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);
        }
    }

    public EntitySelector parse() throws CommandSyntaxException {
        if (this.reader.canRead() && this.reader.peek() == '@') {
            this.reader.skip();
            this.parseSelector();
        } else {
            // Temporary; change to allow entity names/class instance IDs
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);
        }
        return new EntitySelector(this.maxResults, this.includesEntities, this.order, this.limitToType, this.currentEntity);

//        if (reader.canRead()) {
//            switch (reader.peek()) {
//                case 'a':
//                    reader.skip();
//                    if (singleEntity) {
//                        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(), () -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.single_entity"));
//                    }
//                    return new EntitySelector(singleEntity, playerOnly, EntitySelector.EntitySelectors.ALL_PLAYERS);
//                case 'e':
//                    reader.skip();
//                    if (playerOnly) {
//                        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(), () -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.player_only"));
//                    }
//                    return new EntitySelector(singleEntity, playerOnly, EntitySelector.EntitySelectors.ALL_ENTITIES);
//                case 'r':
//                    reader.skip();
//                    return new EntitySelector(singleEntity, playerOnly, EntitySelector.EntitySelectors.RANDOM_PLAYER);
//                case 'p':
//                    reader.skip();
//                    return new EntitySelector(singleEntity, playerOnly, EntitySelector.EntitySelectors.CLOSEST_PLAYER);
//                case 's':
//                    reader.skip();
//                    return new EntitySelector(singleEntity, playerOnly, EntitySelector.EntitySelectors.SELF);
//            }
//        }
//        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.entity.invalid_selector.generic"));
    }
}
