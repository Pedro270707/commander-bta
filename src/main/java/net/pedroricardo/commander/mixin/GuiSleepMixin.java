package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiSleepMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = GuiSleepMP.class, remap = false)
public class GuiSleepMixin extends GuiChat {
    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        super.init();
    }
}
