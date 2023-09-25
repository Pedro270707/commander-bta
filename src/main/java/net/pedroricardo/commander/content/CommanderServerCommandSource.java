package net.pedroricardo.commander.content;

import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.minecraft.core.net.command.ServerCommandHandler;
import net.minecraft.core.net.command.ServerPlayerCommandSender;
import net.minecraft.core.net.packet.Packet3Chat;
import net.minecraft.core.util.helper.AES;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommanderServerCommandSource implements CommanderCommandSource {
    public final MinecraftServer server;
    public final EntityPlayerMP player;

    public CommanderServerCommandSource(MinecraftServer server, EntityPlayerMP player) {
        this.server = server;
        this.player = player;
    }

    @Override
    public Collection<String> getPlayerNames() {
        List<String> list = new ArrayList<>();
        for (EntityPlayer player : server.configManager.playerEntities) {
            list.add(player.username);
        }
        return list;
    }

    @Override
    public Collection<String> getEntitySuggestions() {
        return this.getPlayerNames();
    }

    @Override
    public String toString() {
        return "CommanderServerCommandSource{" + this.server + ", " + this.player + "}";
    }

    @Override
    public @NotNull EntityPlayer getSender() {
        return this.player;
    }

    @Override
    public boolean hasAdmin() {
        return false;
    }

    @Override
    public @NotNull Vec3d getCoordinates(boolean offsetHeight) {
        Vec3d position = this.getSender().getPosition(1.0f);
        if (offsetHeight) return position.addVector(0.0f, -this.getSender().heightOffset, 0.0f);
        return position;
    }

    @Override
    public @NotNull Vec3d getBlockCoordinates() {
        return this.getCoordinates(true);
    }

    @Override
    public void sendMessage(String message) {
        this.player.playerNetServerHandler.sendPacket(new Packet3Chat(message, AES.keyChain.get(this.player.username)));
    }

    @Override
    public void sendMessageToPlayer(EntityPlayer player, String message) {
        this.server.configManager.sendPacketToPlayer(player.username, new Packet3Chat(message, AES.keyChain.get(player.username)));
    }

    @Override
    public World getWorld() {
        return this.player == null ? this.server.getWorldManager(0) : this.player.world;
    }

    @Override
    public World getWorld(int dimension) {
        return this.server.getWorldManager(dimension);
    }

    @Override
    public @Deprecated CommandHandler getCommandHandler() {
        return this.server.serverCommandHandler;
    }

    @Override
    public @Deprecated CommandSender getCommandSender() {
        return new ServerPlayerCommandSender(this.server, this.player);
    }
}
