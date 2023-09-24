package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
    private final Function<Vec3d, Vec3d> position;
    private final AABB aABB;

    public EntitySelector(int maxResults, boolean includesEntities, BiConsumer<Entity, List<? extends Entity>> order, @Nullable Class<? extends Entity> limitToType, boolean typeInverse, boolean currentEntity, Predicate<Entity> predicate, @Nullable String entityId, @Nullable String playerName, MinMaxBounds.Doubles distance, Function<Vec3d, Vec3d> position, AABB aABB) {
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
        this.position = position;
        this.aABB = aABB;
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

        Vec3d sourceCoordinates = source.getCoordinates();
        Vec3d position;
        if (sourceCoordinates != null) {
            position = this.position.apply(sourceCoordinates);
            this.aABB.minX = this.aABB.minX + position.xCoord;
            this.aABB.maxX = this.aABB.maxX + position.xCoord;
            this.aABB.minY = this.aABB.minY + position.yCoord;
            this.aABB.maxY = this.aABB.maxY + position.yCoord;
            this.aABB.minZ = this.aABB.minZ + position.zCoord;
            this.aABB.maxZ = this.aABB.maxZ + position.zCoord;
        } else {
            position = this.position.apply(Vec3d.createVector(0, 0, 0));
        }

        Commander.LOGGER.info(position.toString());
        Commander.LOGGER.info(this.aABB.toString());

        List<? extends Entity> temp = new ArrayList<>(entities);
        for (Entity entity : entities) {
            if ((limitToType != null && limitToType.isInstance(entity) == this.typeInverse)
                    || !predicate.test(entity)
                    || !this.distanceContains(entity, position.xCoord, position.yCoord, position.zCoord)
                    || !this.aABB.intersectsWith(entity.bb)) {
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

    private boolean distanceContains(Entity entity, double x, double y, double z) {
        if (this.distance.isAny()) return true;
        return this.distance.contains(entity.distanceTo(x, y, z));
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
