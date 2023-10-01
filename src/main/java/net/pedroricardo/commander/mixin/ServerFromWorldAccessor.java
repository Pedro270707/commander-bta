package net.pedroricardo.commander.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WorldServer.class, remap = false)
public interface ServerFromWorldAccessor {
    @Accessor("mcServer")
    MinecraftServer mcServer();
}
