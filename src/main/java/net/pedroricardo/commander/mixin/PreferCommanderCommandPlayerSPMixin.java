package net.pedroricardo.commander.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.EntityPlayerSP;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.Packet3Chat;
import net.minecraft.core.util.helper.AES;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.net.handler.NetServerHandler;
import net.pedroricardo.commander.commands.CommanderClientCommandSource;
import net.pedroricardo.commander.commands.CommanderCommandManager;
import net.pedroricardo.commander.commands.CommanderServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayerSP.class, remap = false)
public class PreferCommanderCommandPlayerSPMixin {
    @Mixin(value = EntityPlayerSP.class, remap = false)
    private interface EntityPlayerSPAccessor {
        @Accessor("mc")
        Minecraft mc();
    }

    @Inject(method = "sendChatMessage", at = @At(value = "INVOKE", target = "Ljava/lang/System;arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V", ordinal = 0, shift = At.Shift.BEFORE),
            cancellable = true)
    private void sendChatMessage(String s, CallbackInfo ci) {
        String command = s.substring(1);
        CommanderClientCommandSource clientCommandSource = new CommanderClientCommandSource(((EntityPlayerSPAccessor)((EntityPlayerSP)(Object)this)).mc());
        try {
            CommanderCommandManager.execute(command, clientCommandSource);
            ci.cancel();
        } catch (CommandSyntaxException e) {
            if (e.getType() != CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand())
                ((EntityPlayerSPAccessor)((EntityPlayerSP)(Object)this)).mc().thePlayer.sender.sendMessage(TextFormatting.RED + e.getMessage());
        }
    }
}