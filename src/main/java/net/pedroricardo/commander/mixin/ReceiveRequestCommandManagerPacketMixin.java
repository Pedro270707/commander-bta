package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.content.CommanderServerCommandSource;
import net.pedroricardo.commander.content.RequestCommandManagerPacket;
import net.pedroricardo.commander.duck.ClassWithManager;
import net.pedroricardo.commander.duck.RequestCommandManagerPacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.SERVER)
@Mixin(value = NetServerHandler.class, remap = false)
public abstract class ReceiveRequestCommandManagerPacketMixin extends NetHandler implements RequestCommandManagerPacketHandler {
    @Shadow private MinecraftServer mcServer;

    @Override
    public void commander$handleRequestCommandManagerPacket(RequestCommandManagerPacket packet) {
        EntityPlayerMP player = this.mcServer.playerList.getPlayerEntity(packet.username);
        player.playerNetServerHandler.sendPacket(new CommandManagerPacket(((ClassWithManager)this.mcServer.getDimensionWorld(player.dimension)).getManager().getDispatcher(), new CommanderServerCommandSource(this.mcServer, player), packet.text, packet.cursor));
    }
}
