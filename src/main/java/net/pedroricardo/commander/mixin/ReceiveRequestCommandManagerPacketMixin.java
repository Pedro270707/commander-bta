package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.content.CommanderServerCommandSource;
import net.pedroricardo.commander.content.RequestCommandManagerPacket;
import net.pedroricardo.commander.duck.EnvironmentWithManager;
import net.pedroricardo.commander.duck.RequestCommandManagerPacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.SERVER)
@Mixin(NetServerHandler.class)
public abstract class ReceiveRequestCommandManagerPacketMixin extends NetHandler implements RequestCommandManagerPacketHandler {
    @Shadow private MinecraftServer mcServer;

    @Override
    public void commander$handleRequestCommandManagerPacket(RequestCommandManagerPacket packet) {
        EntityPlayerMP player = this.mcServer.playerList.getPlayerEntity(packet.username);
        player.playerNetServerHandler.sendPacket(new CommandManagerPacket(((EnvironmentWithManager)this.mcServer).getManager().getDispatcher(), new CommanderServerCommandSource(this.mcServer, player), packet.text, packet.cursor));
    }
}
