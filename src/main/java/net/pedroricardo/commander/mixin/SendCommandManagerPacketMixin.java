package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.net.packet.Packet1Login;
import net.minecraft.core.world.chunk.ChunkCoordinates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetLoginHandler;
import net.minecraft.server.net.handler.NetServerHandler;
import net.minecraft.server.world.WorldServer;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.content.CommanderServerCommandSource;
import net.pedroricardo.commander.duck.EnvironmentWithManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.SERVER)
@Mixin(value = NetLoginHandler.class, remap = false)
public class SendCommandManagerPacketMixin {
    @Shadow private MinecraftServer mcServer;

    @Inject(method = "doLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/net/handler/NetServerHandler;sendPacket(Lnet/minecraft/core/net/packet/Packet;)V", ordinal = 7), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void commander$sendCommandManagerPacket(Packet1Login packet1login, CallbackInfo ci, EntityPlayerMP player) {
        player.playerNetServerHandler.sendPacket(new CommandManagerPacket(((EnvironmentWithManager)this.mcServer).getManager().getDispatcher(), new CommanderServerCommandSource(this.mcServer, player), "", 0));
    }
}
