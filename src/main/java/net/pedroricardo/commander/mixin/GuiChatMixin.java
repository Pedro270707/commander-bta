package net.pedroricardo.commander.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.core.net.command.TextFormatting;
import org.objectweb.asm.Opcodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.pedroricardo.commander.GuiHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.duck.EnvironmentWithManager;
import net.pedroricardo.commander.gui.GuiChatSuggestions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GuiChat.class, remap = false)
public class GuiChatMixin {
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

    @Unique
    private GuiChatSuggestions suggestionsGui;
    @Unique
    private @Nullable ParseResults<CommanderCommandSource> parseResults;
    @Unique
    private final List<String> ARGUMENT_STYLES = new ArrayList<>();

    @Inject(method = "initGui", at = @At("TAIL"))
    private void initGui(CallbackInfo ci) {
        this.suggestionsGui = new GuiChatSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor(), (GuiChat)(Object)this);
        this.ARGUMENT_STYLES.add(TextFormatting.LIGHT_BLUE.toString());
        this.ARGUMENT_STYLES.add(TextFormatting.YELLOW.toString());
        this.ARGUMENT_STYLES.add(TextFormatting.LIME.toString());
        this.ARGUMENT_STYLES.add(TextFormatting.PINK.toString());
        this.ARGUMENT_STYLES.add(TextFormatting.ORANGE.toString());
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void drawSuggestionPreview(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        int mouseX = GuiHelper.getScaledMouseX(((GuiScreenAccessor)((GuiChat)(Object)this)).mc());
        int mouseY = GuiHelper.getScaledMouseY(((GuiScreenAccessor)((GuiChat)(Object)this)).mc()) - 1;

        if (!this.suggestionsGui.getSuggestions().isEmpty()
                && this.suggestionsGui.getParseResults() != null
                && (this.suggestionsGui.getCursor() == this.suggestionsGui.getParseResults().getReader().getString().trim().length()
                    || this.suggestionsGui.getCursor() == this.suggestionsGui.getParseResults().getReader().getString().length())) {
            Suggestion suggestionToRender;
            if (this.suggestionsGui.isHoveringOverSuggestions(mouseX, mouseY)) {
                suggestionToRender = this.suggestionsGui.getSuggestions().get(this.suggestionsGui.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get());
            } else {
                suggestionToRender = this.suggestionsGui.getSuggestions().get(0);
            }

            if (suggestionToRender.getText().startsWith(((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor().getText().substring(suggestionToRender.getRange().getStart()))) {
                int leftMargin = 3 + ((GuiScreenAccessor) ((GuiChat) (Object) this)).fontRenderer().getStringWidth(this.suggestionsGui.getMessage().substring(0, suggestionToRender.getRange().getStart()));
                ((GuiScreenAccessor) ((GuiChat) (Object) this)).fontRenderer().drawStringWithShadow(TextFormatting.LIGHT_GRAY + suggestionToRender.getText(), leftMargin + 1, ((GuiChat) (Object) this).height - 12, 0xE0E0E0);
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void drawSuggestionsGuiScreen(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        this.suggestionsGui.drawScreen();
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    private void keyTyped(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        this.suggestionsGui.keyTyped(c, key);
    }

    @Inject(method = "keyTyped", at = @At(value = "JUMP", ordinal = 1), cancellable = true)
    private void upArrowPressed(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.suggestionsGui.getCommandIndex() != -1) {
            this.suggestionsGui.cycleThroughSuggestions(-1);
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "JUMP", ordinal = 5), cancellable = true)
    private void downArrowPressed(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.suggestionsGui.getCommandIndex() != -1) {
            this.suggestionsGui.cycleThroughSuggestions();
            ci.cancel();
        }
    }

    @Inject(method = "updateScreen", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void updateScreen(CallbackInfo ci, int dWheel) {
        this.suggestionsGui.updateScreen(dWheel);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int x, int y, int button, CallbackInfo ci) {
        this.suggestionsGui.mouseClicked(x, y, button);
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0))
    private void drawString(GuiChat instance, FontRenderer fontRenderer, String text, int x, int y, int argb) {
        int cursor = ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor().getCursor();

        if (this.parseResults != null && !this.parseResults.getReader().getString().equals(text)) {
            this.parseResults = null;
        }

        StringReader stringReader = new StringReader(text);
        boolean isCommand = stringReader.canRead() && stringReader.peek() == '/';
        if (isCommand) {
            stringReader.skip();
            CommandDispatcher<CommanderCommandSource> dispatcher = this.suggestionsGui.getManager().getDispatcher();
            if (this.parseResults == null) {
                this.parseResults = dispatcher.parse(stringReader, this.suggestionsGui.getCommandSource());
            }

            int distanceFromCursorToTextStart = 0;

            StringBuilder stringToDrawBuilder = new StringBuilder();
            CommandContextBuilder<CommanderCommandSource> builder = this.parseResults.getContext().getLastChild();
            int n;
            int j = 0;
            int k = -1;
            for (ParsedArgument<CommanderCommandSource, ?> parsedArgument : builder.getArguments().values()) {
                int l;
                if (++k >= ARGUMENT_STYLES.size()) {
                    k = 0;
                }
                if ((l = Math.max(parsedArgument.getRange().getStart() - distanceFromCursorToTextStart, 0)) >= text.length()) break;
                int m = Math.min(parsedArgument.getRange().getEnd() - distanceFromCursorToTextStart, text.length());
                if (m <= 0) continue;
                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, j, l);
                stringToDrawBuilder.append(ARGUMENT_STYLES.get(k)).append(text, l, m);
                j = m;
            }
            if (this.parseResults.getReader().canRead() && (n = Math.max(this.parseResults.getReader().getCursor() - distanceFromCursorToTextStart, 0)) < text.length()) {
                int o = Math.min(n + this.parseResults.getReader().getRemainingLength(), text.length());
                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, j, n);
                stringToDrawBuilder.append(TextFormatting.RED).append(text, n, o);
                j = o;
            }
            stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text.substring(j));
            instance.drawString(fontRenderer, stringToDrawBuilder.toString(), x, y, argb);
        } else {
            instance.drawString(fontRenderer, text, x, y, argb);
        }
    }
}
