package net.pedroricardo.commander.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.net.packet.Packet3Chat;
import net.minecraft.core.util.helper.AES;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;
import net.pedroricardo.commander.commands.CommanderCommandManager;
import net.pedroricardo.commander.commands.CommanderServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetServerHandler.class, remap = false)
public class PreferCommanderCommandNetServerHandlerMixin {
    @Mixin(value = NetServerHandler.class, remap = false)
    private interface NetServerHandlerAccessor {
        @Accessor("mcServer")
        MinecraftServer mcServer();

        @Accessor("playerEntity")
        EntityPlayerMP playerEntity();
    }

    @Inject(method = "handleSlashCommand", at = @At("HEAD"), cancellable = true)
    private void handleSlashCommand(String s, CallbackInfo ci) {
        CommanderServerCommandSource serverCommandSource = new CommanderServerCommandSource(((NetServerHandlerAccessor)((NetServerHandler)(Object)this)).mcServer(), ((NetServerHandlerAccessor)((NetServerHandler)(Object)this)).playerEntity());
        try {
            CommanderCommandManager.execute(s, serverCommandSource);
            ci.cancel();
        } catch (CommandSyntaxException e) {
            if (e.getType() != CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand())
                ((NetServerHandlerAccessor)((NetServerHandler)(Object)this)).playerEntity().playerNetServerHandler.sendPacket(new Packet3Chat(e.getMessage(), AES.keyChain.get(((NetServerHandlerAccessor)((NetServerHandler)(Object)this)).playerEntity().username)));
        }
    }
}
