package net.pedroricardo.commander.content.helpers;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.Commander;
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
    private String playerName;

    public EntitySelectorParser(StringReader reader) {
        this.reader = reader;
    }

    private void parseSelector() throws CommandSyntaxException {
        this.reader.skip();
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
            default:
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);
        }
        reader.skip();
        // TODO: add target selector extra arguments or whatever it's called
    }

    private void parseName() throws CommandSyntaxException {
        this.maxResults = 1;
        this.includesEntities = false;
        this.playerName = this.reader.readString();
    }

    public EntitySelector parse() throws CommandSyntaxException {
        if (!this.reader.canRead()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);

        if (this.reader.peek() == '@') {
            this.parseSelector();
        } else {
            this.parseName();
        }
        return new EntitySelector(this.maxResults, this.includesEntities, this.order, this.limitToType, this.currentEntity, this.playerName);
    }
}
