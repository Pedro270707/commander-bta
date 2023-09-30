package net.pedroricardo.commander.content;

import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.minecraft.core.net.command.ConsoleCommandSender;
import net.minecraft.core.net.command.ServerPlayerCommandSender;
import net.minecraft.core.net.packet.Packet3Chat;
import net.minecraft.core.util.helper.AES;
import net.minecraft.core.util.helper.LogPrintStream;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.pedroricardo.commander.Commander;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommanderConsoleCommandSource implements CommanderCommandSource {
    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    public final MinecraftServer server;

    public CommanderConsoleCommandSource(MinecraftServer server) {
        this.server = server;
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
        return "CommanderConsoleCommandSource{" + this.server + "}";
    }

    @Override
    public EntityPlayer getSender() {
        return null;
    }

    @Override
    public boolean hasAdmin() {
        return true;
    }

    @Override
    public @Nullable Vec3d getCoordinates(boolean offsetHeight) {
        return null;
    }

    @Override
    public @Nullable Vec3d getBlockCoordinates() {
        return null;
    }

    @Override
    public boolean messageMayBeMultiline() {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        LOGGER.info(LogPrintStream.removeColorCodes(message));
    }

    @Override
    public void sendMessageToPlayer(EntityPlayer player, String message) {
        this.server.configManager.sendPacketToPlayer(player.username, new Packet3Chat(message, AES.keyChain.get(player.username)));
    }

    @Override
    public World getWorld() {
        return this.server.getWorldManager(0);
    }

    @Override
    public World getWorld(int dimension) {
        return this.server.getWorldManager(dimension);
    }

    @Override
    public void movePlayerToDimension(EntityPlayer player, int dimension) {
        if (player instanceof EntityPlayerMP) this.server.configManager.sendPlayerToOtherDimension((EntityPlayerMP) player, dimension);
        throw new IllegalStateException("Player is not an instance of EntityPlayerMP");
    }

    @Override
    public @Deprecated CommandHandler getCommandHandler() {
        return this.server.serverCommandHandler;
    }

    @Override
    public @Deprecated CommandSender getCommandSender() {
        return new ConsoleCommandSender(this.server);
    }
}