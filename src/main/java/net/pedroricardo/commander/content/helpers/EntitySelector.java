package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class EntitySelector {
    private final int maxResults;
    private final boolean includesEntities;
    private final BiConsumer<Entity, List<? extends Entity>> order;
    private final @Nullable Class<? extends Entity> limitToType;
    private final boolean currentEntity;
    private final @Nullable String playerName;

    public EntitySelector(int maxResults, boolean includesEntities, BiConsumer<Entity, List<? extends Entity>> order, @Nullable Class<? extends Entity> limitToType, boolean currentEntity, @Nullable String playerName) {
        this.maxResults = maxResults;
        this.includesEntities = includesEntities;
        this.order = order;
        this.limitToType = limitToType;
        this.currentEntity = currentEntity;
        this.playerName = playerName;
    }

    public List<? extends Entity> get(CommanderCommandSource commandSource) throws CommandSyntaxException {
        if (this.playerName != null) {
            List<EntityPlayer> players = new ArrayList<>();
            for (EntityPlayer player : commandSource.getSender().world.players) {
                if (player.username.equals(this.playerName) || player.nickname.equals(this.playerName)) {
                    players.add(player);
                }
            }
            return players.subList(0, Math.min(players.size(), this.maxResults));
        }

        List<? extends Entity> entities;
        if (this.includesEntities) {
            entities = commandSource.getSender().world.loadedEntityList;
        } else {
            entities = commandSource.getSender().world.players;
        }
        if (limitToType != null) {
            List<? extends Entity> temp = new ArrayList<>(entities);
            for (Entity entity : entities) {
                if (!limitToType.isInstance(entity)) {
                    temp.remove(entity);
                }
            }
            entities = temp;
        }
        this.order.accept(commandSource.getSender(), entities);
        if (this.maxResults < entities.size()) {
            entities = entities.subList(0, this.maxResults);
        }
        return entities;
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
