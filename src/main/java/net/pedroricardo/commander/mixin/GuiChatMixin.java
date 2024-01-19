package net.pedroricardo.commander.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.hud.ComponentAnchor;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.render.FontRenderer;
import net.pedroricardo.commander.gui.GuiChatSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = GuiChat.class, remap = false)
public abstract class GuiChatMixin {
    @Shadow public abstract String getText();

    @Unique
    private GuiChatSuggestions commander$suggestionsGui;

    @Inject(method = "init", at = @At("TAIL"))
    private void initGui(CallbackInfo ci) {
        this.commander$suggestionsGui = new GuiChatSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor(), (GuiChat)(Object)this, (parent, child, minecraft, followParameters) -> 16 + (followParameters ? this.commander$suggestionsGui.getDefaultParameterPosition() - 1 : 0), (parent, child, minecraft, followParameters) -> minecraft.resolution.scaledHeight - 14, ComponentAnchor.BOTTOM_LEFT);
        if (!this.getText().startsWith("/")) this.commander$suggestionsGui.hidden = true;
        else this.commander$suggestionsGui.updateSuggestions();
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void drawSuggestionPreview(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        ((GuiScreenAccessor)(Object)this).fontRenderer().drawStringWithShadow(this.commander$suggestionsGui.getSuggestionPreview(), 16 + this.commander$suggestionsGui.getDefaultParameterPosition(), ((GuiScreenAccessor)((GuiChat)(Object)this)).mc().resolution.scaledHeight - 12, 16777215);
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void drawSuggestionsGuiScreen(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) this.commander$suggestionsGui.drawScreen();
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    private void keyTyped(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) this.commander$suggestionsGui.keyTyped(c, key);
    }

    @Inject(method = "keyTyped", at = @At(value = "JUMP", ordinal = 1), cancellable = true)
    private void upArrowPressed(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) {
            if (this.commander$suggestionsGui.getCommandIndex() != -1) {
                this.commander$suggestionsGui.cycleThroughSuggestions(-1);
                ci.cancel();
            }
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/text/TextFieldEditor;setCursor(I)V", ordinal = 1))
    private void commander$upArrowPressedUpdateSuggestions(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) {
            this.commander$suggestionsGui.updateSuggestions();
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "JUMP", ordinal = 5), cancellable = true)
    private void downArrowPressed(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) {
            if (this.commander$suggestionsGui.getCommandIndex() != -1) {
                this.commander$suggestionsGui.cycleThroughSuggestions();
                ci.cancel();
            }
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/text/TextFieldEditor;setCursor(I)V", ordinal = 4))
    private void commander$downArrowPressedUpdateSuggestions(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) {
            this.commander$suggestionsGui.updateSuggestions();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void tick(CallbackInfo ci, int dWheel) {
        if (this.commander$suggestionsGui != null) {
            this.commander$suggestionsGui.hidden = !this.getText().startsWith("/");
            this.commander$suggestionsGui.updateScreen(dWheel);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int x, int y, int button, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) this.commander$suggestionsGui.mouseClicked(x, y, button);
    }

    @WrapOperation(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0))
    private void commander$colorCodeChatText(GuiChat instance, FontRenderer fontRenderer, String text, int x, int y, int argb, Operation<Void> original) {
        original.call(instance, fontRenderer, this.commander$suggestionsGui.colorCodeText(text, true), x, y, argb);
    }
}
