package net.pedroricardo.commander.duck;

import net.pedroricardo.commander.content.RequestCommandManagerPacket;

public interface RequestCommandManagerPacketHandler {
    void commander$handleRequestCommandManagerPacket(RequestCommandManagerPacket packet);
}
