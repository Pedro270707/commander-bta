package net.pedroricardo.commander.commands;

import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;

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
    public String getType() {
        return "server";
    }

    @Override
    public EntityPlayer getSender() {
        return this.player;
    }

    @Override
    public boolean hasAdmin() {
        return false;
    }
}
