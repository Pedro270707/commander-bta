package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.window.GameWindow;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.minecraft.core.world.save.LevelStorage;
import net.minecraft.core.world.type.WorldType;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.duck.ClassWithManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class, remap = false)
public class WorldManagerMixin implements ClassWithManager {
    @Unique
    private CommanderCommandManager COMMAND_MANAGER;

    @Inject(method = "<init>(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/Dimension;)V", at = @At("TAIL"))
    private void init1(World world, Dimension dimension, CallbackInfo ci) {
        this.COMMAND_MANAGER = new CommanderCommandManager(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER);
        this.COMMAND_MANAGER.init();
    }

    @Inject(method = "<init>(Lnet/minecraft/core/world/save/LevelStorage;Ljava/lang/String;Lnet/minecraft/core/world/Dimension;Lnet/minecraft/core/world/type/WorldType;J)V", at = @At("TAIL"))
    private void init2(LevelStorage saveHandler, String name, Dimension dimension, WorldType worldType, long seed, CallbackInfo ci) {
        this.COMMAND_MANAGER = new CommanderCommandManager(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER);
        this.COMMAND_MANAGER.init();
    }

    @Inject(method = "<init>(Lnet/minecraft/core/world/save/LevelStorage;Ljava/lang/String;JLnet/minecraft/core/world/Dimension;Lnet/minecraft/core/world/type/WorldType;)V", at = @At("TAIL"))
    private void init3(LevelStorage saveHandler, String name, long seed, Dimension dimension, WorldType worldType, CallbackInfo ci) {
        this.COMMAND_MANAGER = new CommanderCommandManager(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER);
        this.COMMAND_MANAGER.init();
    }

    @Override
    public CommanderCommandManager getManager() {
        return this.COMMAND_MANAGER;
    }
}
