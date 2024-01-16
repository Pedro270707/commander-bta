package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.net.handler.NetClientHandler;
import net.minecraft.core.net.handler.NetHandler;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.duck.CommandManagerPacketHandler;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(NetClientHandler.class)
public abstract class ReceiveCommandManagerPacketMixin extends NetHandler implements CommandManagerPacketHandler {
    @Override
    public void commander$handleCommandManagerPacket(CommandManagerPacket packet) {
        Commander.serverSuggestions = packet.suggestions;
    }
}
