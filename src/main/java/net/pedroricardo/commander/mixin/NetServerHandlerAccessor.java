package net.pedroricardo.commander.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.net.handler.NetServerHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NetServerHandler.class, remap = false)
public interface NetServerHandlerAccessor {
    @Accessor("mcServer")
    MinecraftServer mcServer();
}
