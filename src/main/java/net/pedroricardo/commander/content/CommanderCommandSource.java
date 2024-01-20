package net.pedroricardo.commander.content;

import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
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

    String toString();

    @Nullable EntityPlayer getSender();

    boolean hasAdmin();

    @Nullable Vec3d getCoordinates(boolean offsetHeight);

    @Nullable Vec3d getBlockCoordinates();

    boolean messageMayBeMultiline();

    void sendMessage(String message);

    void sendMessage(EntityPlayer player, String message);

    default void sendTranslatableMessage(String message, Object... args) {
        this.sendMessage(I18n.getInstance().translateKeyAndFormat(message, args));
    }

    default void sendTranslatableMessage(EntityPlayer player, String message, Object... args) {
        this.sendMessage(player, I18n.getInstance().translateKeyAndFormat(message, args));
    }

    void sendMessageToAllPlayers(String message);

    World getWorld();

    World getWorld(int dimension);

    void movePlayerToDimension(EntityPlayer player, int dimension);

    String getName();

    @Deprecated CommandHandler getCommandHandler();

    @Deprecated CommandSender getCommandSender();
}
