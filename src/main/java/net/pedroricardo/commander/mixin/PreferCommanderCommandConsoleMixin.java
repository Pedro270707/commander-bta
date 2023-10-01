package net.pedroricardo.commander.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.EntityPlayerSP;
import net.minecraft.core.net.ServerCommand;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.MinecraftServer;
import net.pedroricardo.commander.content.CommanderClientCommandSource;
import net.pedroricardo.commander.content.CommanderConsoleCommandSource;
import net.pedroricardo.commander.duck.EnvironmentWithManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = MinecraftServer.class, remap = false)
public class PreferCommanderCommandConsoleMixin {
    @Inject(method = "commandLineParser", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/net/ConsoleCommandHandler;handleCommand(Lnet/minecraft/core/net/ServerCommand;)V"),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void commandLineParser(CallbackInfo ci, ServerCommand serverCommand) {
        String command = serverCommand.command;
        CommanderConsoleCommandSource source = new CommanderConsoleCommandSource((MinecraftServer)(Object)this);
        try {
            ((EnvironmentWithManager)((MinecraftServer)(Object)this)).getManager().execute(command, source);
        } catch (CommandSyntaxException e) {
            source.sendMessage(e.getMessage());
        }
        ci.cancel();
    }
}
