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
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.render.FontRenderer;
import net.pedroricardo.commander.GuiHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.gui.GuiChatSuggestions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = GuiChat.class, remap = false)
public class GuiChatMixin {
    @Unique
    private GuiChatSuggestions commander$suggestionsGui;
    @Unique
    private @Nullable ParseResults<CommanderCommandSource> parseResults;

    @Inject(method = "init", at = @At("TAIL"))
    private void initGui(CallbackInfo ci) {
        this.commander$suggestionsGui = new GuiChatSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor(), (GuiChat)(Object)this);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void drawSuggestionPreview(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        int mouseX = GuiHelper.getScaledMouseX(((GuiScreenAccessor)((GuiChat)(Object)this)).mc());
        int mouseY = GuiHelper.getScaledMouseY(((GuiScreenAccessor)((GuiChat)(Object)this)).mc()) - 1;

        if (this.shouldDrawSuggestionPreview()) {
            Suggestion suggestionToRender;
            if (this.commander$suggestionsGui.isHoveringOverSuggestions(mouseX, mouseY)) {
                suggestionToRender = this.commander$suggestionsGui.getSuggestions().get(this.commander$suggestionsGui.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get());
            } else {
                suggestionToRender = this.commander$suggestionsGui.getSuggestions().get(0);
            }

            if (suggestionToRender.getText().startsWith(((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor().getText().substring(Math.min(suggestionToRender.getRange().getStart(), ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor().getText().length())))) {
                int leftMargin = 17 + ((GuiScreenAccessor) ((GuiChat) (Object) this)).fontRenderer().getStringWidth(this.commander$suggestionsGui.getMessage().substring(0, Math.min(suggestionToRender.getRange().getStart(), this.commander$suggestionsGui.getMessage().length())));
                ((GuiScreenAccessor) ((GuiChat) (Object) this)).fontRenderer().drawStringWithShadow(TextFormatting.LIGHT_GRAY + suggestionToRender.getText(), leftMargin + 1, ((GuiChat) (Object) this).height - 12, 0xE0E0E0);
            }
        }
    }

    @Unique
    private boolean shouldDrawSuggestionPreview() {
        boolean basedOnParseResults = this.commander$suggestionsGui.getParseResults() != null && (this.commander$suggestionsGui.getCursor() == this.commander$suggestionsGui.getParseResults().getReader().getString().trim().length() || this.commander$suggestionsGui.getCursor() == this.commander$suggestionsGui.getParseResults().getReader().getString().length());
        boolean basedOnServer = Commander.serverSuggestions.has("reader") && (this.commander$suggestionsGui.getCursor() == Commander.serverSuggestions.getAsJsonObject("reader").get("string").getAsString().trim().length() || this.commander$suggestionsGui.getCursor() == Commander.serverSuggestions.getAsJsonObject("reader").get("string").getAsString().length());
        return basedOnParseResults || basedOnServer;
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
        if (this.commander$suggestionsGui != null && this.commander$suggestionsGui.getCommandIndex() != -1) {
            this.commander$suggestionsGui.cycleThroughSuggestions(-1);
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "JUMP", ordinal = 5), cancellable = true)
    private void downArrowPressed(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null && this.commander$suggestionsGui.getCommandIndex() != -1) {
            this.commander$suggestionsGui.cycleThroughSuggestions();
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void tick(CallbackInfo ci, int dWheel) {
        if (this.commander$suggestionsGui != null) this.commander$suggestionsGui.updateScreen(dWheel);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int x, int y, int button, CallbackInfo ci) {
        if (this.commander$suggestionsGui != null) this.commander$suggestionsGui.mouseClicked(x, y, button);
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0))
    private void drawString(GuiChat instance, FontRenderer fontRenderer, String text, int x, int y, int argb) {
        if (this.parseResults != null && !this.parseResults.getReader().getString().equals(text)) {
            this.parseResults = null;
        }

        StringReader stringReader = new StringReader(text);
        boolean isCommand = stringReader.canRead() && stringReader.peek() == '/';
        if (isCommand) {
            if (!text.isEmpty() && !Commander.serverSuggestions.isEmpty() && Commander.serverSuggestions.get("last_child") != null && Commander.serverSuggestions.get("last_child").getAsJsonObject().get("arguments") != null) {
                StringBuilder stringToDrawBuilder = new StringBuilder();
                int currentArgumentEnd = 1;
                int currentColor = 0;
                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text.charAt(0));
                for (JsonElement jsonElement : Commander.serverSuggestions.getAsJsonObject("last_child").getAsJsonArray("arguments")) {
                    int rangeStart = jsonElement.getAsJsonObject().getAsJsonObject("range").get("start").getAsInt();
                    int rangeEnd = jsonElement.getAsJsonObject().getAsJsonObject("range").get("end").getAsInt();

                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, Math.min(currentArgumentEnd, text.length()), Math.min(rangeStart, text.length()));
                    stringToDrawBuilder.append(Commander.ARGUMENT_STYLES.get(currentColor)).append(text, Math.min(rangeStart, text.length()), Math.min(rangeEnd, text.length()));

                    currentArgumentEnd = rangeEnd;
                    if (++currentColor >= Commander.ARGUMENT_STYLES.size()) {
                        currentColor = 0;
                    }
                }
                if (Commander.serverSuggestions.getAsJsonObject("reader").get("can_read").getAsBoolean() && currentArgumentEnd < text.length()) {
                    int remainingTextLength = Commander.serverSuggestions.getAsJsonObject("reader").get("remaining_text_length").getAsInt();
                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, Math.min(Commander.serverSuggestions.getAsJsonObject("reader").get("cursor").getAsInt(), text.length()));
                    stringToDrawBuilder.append(TextFormatting.RED).append(text, Math.min(Commander.serverSuggestions.getAsJsonObject("reader").get("cursor").getAsInt(), text.length()), Math.min(remainingTextLength, text.length()));
                    currentArgumentEnd = remainingTextLength;
                }
                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text.substring(Math.min(currentArgumentEnd, text.length())));

                instance.drawString(fontRenderer, stringToDrawBuilder.toString(), x, y, argb);
            } else {
                stringReader.skip();
                CommandDispatcher<CommanderCommandSource> dispatcher = this.commander$suggestionsGui.getManager().getDispatcher();
                if (this.parseResults == null) {
                    this.parseResults = dispatcher.parse(stringReader, this.commander$suggestionsGui.getCommandSource());
                }
                StringBuilder stringToDrawBuilder = new StringBuilder();
                CommandContextBuilder<CommanderCommandSource> builder = this.parseResults.getContext().getLastChild();
                int readerCursor;
                int currentArgumentEnd = 0;
                int currentColor = -1;
                for (ParsedArgument<CommanderCommandSource, ?> parsedArgument : builder.getArguments().values()) {
                    int rangeStart;
                    if (++currentColor >= Commander.ARGUMENT_STYLES.size()) {
                        currentColor = 0;
                    }
                    if ((rangeStart = Math.max(parsedArgument.getRange().getStart(), 0)) >= text.length()) break;
                    int rangeEnd = Math.min(parsedArgument.getRange().getEnd(), text.length());
                    if (rangeEnd <= 0) continue;
                    stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, rangeStart);
                    stringToDrawBuilder.append(Commander.ARGUMENT_STYLES.get(currentColor)).append(text, rangeStart, rangeEnd);
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
