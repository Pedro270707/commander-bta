package net.pedroricardo.commander.content;

import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.minecraft.core.util.phys.Vec3d;
import org.jetbrains.annotations.Nullable;

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

    @Nullable Vec3d getCoordinates();

    void sendMessage(String message);
}
