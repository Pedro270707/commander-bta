package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.render.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(value = GuiScreen.class, remap = false)
public interface GuiScreenAccessor {
    @Accessor("fontRenderer")
    FontRenderer fontRenderer();

    @Accessor("mc")
    Minecraft mc();
}