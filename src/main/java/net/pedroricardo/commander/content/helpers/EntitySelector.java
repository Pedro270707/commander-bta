package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class EntitySelector {
    private final int maxResults;
    private final boolean includesEntities;
    private final BiConsumer<Entity, List<? extends Entity>> order;
    private final @Nullable Class<? extends Entity> limitToType;
    private final boolean typeInverse;
    private final boolean currentEntity;
    private final Predicate<Entity> predicate;
    private final @Nullable String entityId;
    private final @Nullable String playerName;
    private final MinMaxBounds.Doubles distance;

    public EntitySelector(int maxResults, boolean includesEntities, BiConsumer<Entity, List<? extends Entity>> order, @Nullable Class<? extends Entity> limitToType, boolean typeInverse, boolean currentEntity, Predicate<Entity> predicate, @Nullable String entityId, @Nullable String playerName, MinMaxBounds.Doubles distance) {
        this.maxResults = maxResults;
        this.includesEntities = includesEntities;
        this.order = order;
        this.limitToType = limitToType;
        this.typeInverse = typeInverse;
        this.currentEntity = currentEntity;
        this.predicate = predicate;
        this.entityId = entityId;
        this.playerName = playerName;
        this.distance = distance;
    }

    public List<? extends Entity> get(CommanderCommandSource source) throws CommandSyntaxException {
        // Entity ID/player
        if (this.entityId != null) {
            List<Entity> entities = new ArrayList<>();
            for (Entity entity : source.getWorld().loadedEntityList) {
                if ((Commander.ENTITY_PREFIX + entity.hashCode()).equals(this.entityId)) {
                    entities.add(entity);
                }
            }
            return entities.subList(0, Math.min(entities.size(), this.maxResults));
        } else if (this.playerName != null) {
            List<EntityPlayer> players = new ArrayList<>();
            for (EntityPlayer player : source.getWorld().players) {
                if (player.username.equals(this.playerName) || player.nickname.equals(this.playerName)) {
                    players.add(player);
                }
            }
            return players.subList(0, Math.min(players.size(), this.maxResults));
        }

        // Player only
        List<? extends Entity> entities;
        if (this.includesEntities) {
            entities = source.getWorld().loadedEntityList;
        } else {
            entities = source.getWorld().players;
        }

        List<? extends Entity> temp = new ArrayList<>(entities);
        for (Entity entity : entities) {
            if ((limitToType != null && limitToType.isInstance(entity) == this.typeInverse)
                    || !predicate.test(entity)
                    || !this.distanceContains(source, entity)) {
                temp.remove(entity);
            }
        }
        entities = temp;

        // Sorting order
        this.order.accept(source.getSender(), entities);

        // Predicate
        List<Entity> listAfterPredicate = new ArrayList<>();
        for (Entity entity : entities) {
            if (!predicate.test(entity)) continue;
            listAfterPredicate.add(entity);
        }

        // Maximum amount of results
        listAfterPredicate = listAfterPredicate.subList(0, Math.min(listAfterPredicate.size(), this.maxResults));

        return listAfterPredicate;
    }

    private boolean distanceContains(CommanderCommandSource source, Entity entity) {
        if (this.distance.isAny()) return true;
        return source.getSender() != null && this.distance.contains(source.getSender().distanceTo(entity));
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public boolean includesEntities() {
        return this.includesEntities;
    }

    public boolean isCurrentEntity() {
        return this.currentEntity;
    }
}
