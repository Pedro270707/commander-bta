package net.pedroricardo.commander.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.text.TextFieldEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(value = GuiChat.class, remap = false)
public interface TextFieldEditorAccessor {
    @Accessor("editor")
    TextFieldEditor editor();
}