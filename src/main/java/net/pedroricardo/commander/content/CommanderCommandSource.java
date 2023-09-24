package net.pedroricardo.commander.content;

import net.minecraft.core.entity.player.EntityPlayer;
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

    void sendMessage(String message);

    World getWorld();

    World getWorld(int dimension);

    @Deprecated CommandHandler getCommandHandler();

    @Deprecated CommandSender getCommandSender();
}
