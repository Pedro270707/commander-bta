package net.pedroricardo.commander.duck;

import net.pedroricardo.commander.content.CommandManagerPacket;

public interface CommandManagerPacketHandler {
    void commander$handleCommandManagerPacket(CommandManagerPacket packet);
}
