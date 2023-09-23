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
    private final boolean currentEntity;
    private final Predicate<Entity> predicate;
    private final @Nullable String entityId;
    private final @Nullable String playerName;

    public EntitySelector(int maxResults, boolean includesEntities, BiConsumer<Entity, List<? extends Entity>> order, @Nullable Class<? extends Entity> limitToType, boolean currentEntity, Predicate<Entity> predicate, @Nullable String entityId, @Nullable String playerName) {
        this.maxResults = maxResults;
        this.includesEntities = includesEntities;
        this.order = order;
        this.limitToType = limitToType;
        this.currentEntity = currentEntity;
        this.predicate = predicate;
        this.entityId = entityId;
        this.playerName = playerName;
    }

    public List<? extends Entity> get(CommanderCommandSource commandSource) throws CommandSyntaxException {
        // Entity ID/player
        if (this.entityId != null) {
            List<Entity> entities = new ArrayList<>();
            for (Entity entity : commandSource.getWorld().loadedEntityList) {
                if ((Commander.ENTITY_PREFIX + entity.hashCode()).equals(this.entityId)) {
                    entities.add(entity);
                }
            }
            return entities.subList(0, Math.min(entities.size(), this.maxResults));
        } else if (this.playerName != null) {
            List<EntityPlayer> players = new ArrayList<>();
            for (EntityPlayer player : commandSource.getWorld().players) {
                if (player.username.equals(this.playerName) || player.nickname.equals(this.playerName)) {
                    players.add(player);
                }
            }
            return players.subList(0, Math.min(players.size(), this.maxResults));
        }

        // Player only?
        List<? extends Entity> entities;
        if (this.includesEntities) {
            entities = commandSource.getWorld().loadedEntityList;
        } else {
            entities = commandSource.getWorld().players;
        }

        // Limit to entity type
        if (limitToType != null) {
            List<? extends Entity> temp = new ArrayList<>(entities);
            for (Entity entity : entities) {
                if (!limitToType.isInstance(entity)) {
                    temp.remove(entity);
                }
            }
            entities = temp;
        }

        // Sorting order
        this.order.accept(commandSource.getSender(), entities);

        List<Entity> listAfterPredicate = new ArrayList<>();
        // Predicate
        for (Entity entity : entities) {
            if (!predicate.test(entity)) continue;
            listAfterPredicate.add(entity);
        }

        // Maximum amount of results
        listAfterPredicate = listAfterPredicate.subList(0, Math.min(listAfterPredicate.size(), this.maxResults));

        return listAfterPredicate;
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
