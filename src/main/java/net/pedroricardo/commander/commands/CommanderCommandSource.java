package net.pedroricardo.commander.commands;

import net.minecraft.core.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.Collections;

public interface CommanderCommandSource {
    Collection<String> getPlayerNames();

    default Collection<String> getChatSuggestions() {
        return this.getPlayerNames();
    }

    default Collection<String> getEntitySuggestions() {
        return Collections.emptyList();
    }

    String getType();

    EntityPlayer getSender();

    boolean hasAdmin();
}
