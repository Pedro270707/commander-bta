package net.pedroricardo.commander.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.pedroricardo.commander.gui.GuiChatSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = GuiChat.class, remap = false)
public class ShowCommandSuggestionsMixin {
    @Mixin(value = GuiScreen.class, remap = false)
    private interface GuiScreenAccessor {
        @Accessor("fontRenderer")
        FontRenderer fontRenderer();

        @Accessor("mc")
        Minecraft mc();
    }

    @Mixin(value = GuiChat.class, remap = false)
    private interface TextFieldEditorAccessor {
        @Accessor("editor")
        TextFieldEditor editor();
    }

    private GuiChatSuggestions suggestionsGui;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void initGui(CallbackInfo ci) {
        this.suggestionsGui = new GuiChatSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor(), (GuiChat)(Object)this);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void drawSuggestionPreview(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        //if (!this.suggestionsGui.getSuggestions().isEmpty() && this.suggestionsGui.getSuggestions().get(0).)

        //int parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(this.suggestionsGui.getMessage(), this.suggestionsGui.getCursor());
        //int leftMargin = CommanderHelper.getLeftMarginForSuggestionsWithParameterIndex(((GuiScreenAccessor)((GuiChat)(Object)this)).fontRenderer(), this.suggestionsGui.getMessage(), parameterInCursorIndex) + 3;
        //if (!this.suggestionsGui.getSuggestions().isEmpty() && this.suggestionsGui.getCommandIndex() == -1 && parameterInCursorIndex == CommanderHelper.getCommandParameterList(this.suggestionsGui.getMessage()).size() - 1) ((GuiScreenAccessor)((GuiChat)(Object)this)).fontRenderer().drawStringWithShadow("§8" + this.suggestionsGui.getSuggestions().get(0), leftMargin + 1, ((GuiChat)(Object)this).height - 12, 0xE0E0E0);
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void drawSuggestionsGuiScreen(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        this.suggestionsGui.drawScreen();
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    private void keyTyped(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        this.suggestionsGui.keyTyped(c, key);
    }

    @Inject(method = "updateScreen", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void updateScreen(CallbackInfo ci, int dWheel) {
        this.suggestionsGui.updateScreen(dWheel);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int x, int y, int button, CallbackInfo ci) {
        this.suggestionsGui.mouseClicked(x, y, button);
    }
}
