package net.pedroricardo.commander.mixin;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.core.net.command.TextFormatting;
import net.pedroricardo.commander.Commander;
import org.objectweb.asm.Opcodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.pedroricardo.commander.GuiHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
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

    @Inject(method = "init", at = @At("TAIL"))
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
                int leftMargin = 17 + ((GuiScreenAccessor) ((GuiChat) (Object) this)).fontRenderer().getStringWidth(this.suggestionsGui.getMessage().substring(0, suggestionToRender.getRange().getStart()));
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

    @Inject(method = "tick", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void tick(CallbackInfo ci, int dWheel) {
        this.suggestionsGui.updateScreen(dWheel);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int x, int y, int button, CallbackInfo ci) {
        this.suggestionsGui.mouseClicked(x, y, button);
    }

    // TODO: fix this on servers
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0))
    private void drawString(GuiChat instance, FontRenderer fontRenderer, String text, int x, int y, int argb) {
        if (this.parseResults != null && !this.parseResults.getReader().getString().equals(text)) {
            this.parseResults = null;
        }

        StringReader stringReader = new StringReader(text);
        boolean isCommand = stringReader.canRead() && stringReader.peek() == '/';
        if (isCommand) {
            if (!Commander.serverSuggestions.isEmpty() && Commander.serverSuggestions.get("last_child").getAsJsonObject().get("arguments") != null) {
                StringBuilder stringToDrawBuilder = new StringBuilder();
//                int currentArgumentEnd = 0;
//                int currentColor = -1;
//                for (JsonElement jsonArgument : Commander.serverSuggestions.get("last_child").getAsJsonObject().get("arguments").getAsJsonArray()) {
//                    int rangeStart;
//                    if (++currentColor >= ARGUMENT_STYLES.size()) {
//                        currentColor = 0;
//                    }
//                    if ((rangeStart = Math.max(jsonArgument.getAsJsonObject().get("range").getAsJsonObject().get("start").getAsInt(), 0)) >= text.length()) break;
//                    int rangeEnd = Math.min(jsonArgument.getAsJsonObject().get("range").getAsJsonObject().get("end").getAsInt(), text.length());
//                    if (rangeEnd <= 0 || rangeEnd <= rangeStart) continue;
//                    System.out.println("a");
//                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, rangeStart);
//                    System.out.println("b");
//                    stringToDrawBuilder.append(ARGUMENT_STYLES.get(currentColor)).append(text, rangeStart, rangeEnd);
//                    currentArgumentEnd = rangeEnd;
//                }
//                if (currentArgumentEnd < text.length()) {
//                    int remainingTextLength = Commander.serverSuggestions.get("last_child").getAsJsonObject().get("remaining_text_length").getAsInt();
//                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, text.length());
//                    if (text.length() < remainingTextLength) {
//                        stringToDrawBuilder.append(TextFormatting.RED).append(text, remainingTextLength, text.length());
//                    }
//                    currentArgumentEnd = remainingTextLength;
//                }
//                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text.substring(currentArgumentEnd));

                stringToDrawBuilder.append(text);
                instance.drawString(fontRenderer, stringToDrawBuilder.toString(), x, y, argb);
            } else {
                stringReader.skip();
                CommandDispatcher<CommanderCommandSource> dispatcher = this.suggestionsGui.getManager().getDispatcher();
                if (this.parseResults == null) {
                    this.parseResults = dispatcher.parse(stringReader, this.suggestionsGui.getCommandSource());
                }

                StringBuilder stringToDrawBuilder = new StringBuilder();
                CommandContextBuilder<CommanderCommandSource> builder = this.parseResults.getContext().getLastChild();
                int readerCursor;
                int currentArgumentEnd = 0;
                int currentColor = -1;
                for (ParsedArgument<CommanderCommandSource, ?> parsedArgument : builder.getArguments().values()) {
                    int rangeStart;
                    if (++currentColor >= ARGUMENT_STYLES.size()) {
                        currentColor = 0;
                    }
                    if ((rangeStart = Math.max(parsedArgument.getRange().getStart(), 0)) >= text.length()) break;
                    int rangeEnd = Math.min(parsedArgument.getRange().getEnd(), text.length());
                    if (rangeEnd <= 0) continue;
                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, rangeStart);
                    stringToDrawBuilder.append(ARGUMENT_STYLES.get(currentColor)).append(text, rangeStart, rangeEnd);
                    currentArgumentEnd = rangeEnd;
                }
                if (this.parseResults.getReader().canRead() && (readerCursor = Math.max(this.parseResults.getReader().getCursor(), 0)) < text.length()) {
                    int remainingTextLength = Math.min(readerCursor + this.parseResults.getReader().getRemainingLength(), text.length());
                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, readerCursor);
                    stringToDrawBuilder.append(TextFormatting.RED).append(text, readerCursor, remainingTextLength);
                    currentArgumentEnd = remainingTextLength;
                }
                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text.substring(currentArgumentEnd));
                instance.drawString(fontRenderer, stringToDrawBuilder.toString(), x, y, argb);
            }
        } else {
            instance.drawString(fontRenderer, text, x, y, argb);
        }
    }
}
