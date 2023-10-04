package net.pedroricardo.commander.content;

import net.minecraft.server.MinecraftServer;

public interface IServerCommandSource {
    MinecraftServer getServer();
}
