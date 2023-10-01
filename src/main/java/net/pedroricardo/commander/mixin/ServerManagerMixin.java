package net.pedroricardo.commander.mixin;

import net.minecraft.server.MinecraftServer;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.duck.EnvironmentWithManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftServer.class, remap = false)
public class ServerManagerMixin implements EnvironmentWithManager {
    @Unique
    private CommanderCommandManager COMMAND_MANAGER;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.COMMAND_MANAGER = new CommanderCommandManager(true);
        this.COMMAND_MANAGER.init();
    }

    @Override
    public CommanderCommandManager getManager() {
        return this.COMMAND_MANAGER;
    }
}