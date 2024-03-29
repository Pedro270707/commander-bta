package net.pedroricardo.commander.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.EntityPlayerSP;
import net.minecraft.core.net.command.TextFormatting;
import net.pedroricardo.commander.content.CommanderClientCommandSource;
import net.pedroricardo.commander.duck.ClassWithManager;
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
            ((ClassWithManager)((EntityPlayerSP)(Object)this).world).getManager().execute(command, clientCommandSource);
        } catch (CommandSyntaxException e) {
            ((EntityPlayerSPAccessor)((EntityPlayerSP)(Object)this)).mc().thePlayer.sender.sendMessage(TextFormatting.RED + e.getMessage());
        }
        ci.cancel();
    }
}
