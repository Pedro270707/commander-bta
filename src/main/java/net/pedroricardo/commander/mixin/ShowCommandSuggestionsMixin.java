package net.pedroricardo.commander.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;
import java.util.ArrayList;

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

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawRect(IIIII)V", ordinal = 0, shift = At.Shift.AFTER))
    public void drawScreen(int x, int y, float renderPartialTicks, CallbackInfo ci) {
        FontRenderer fontRenderer = ((GuiScreenAccessor)((GuiChat)(Object)this)).fontRenderer();

        if (!CommanderGuiManager.currentError.isEmpty()) {
            this.renderSingleSuggestionLine(fontRenderer, "§e" + CommanderGuiManager.currentError);
        } else if (!CommanderGuiManager.suggestions.isEmpty()) {
            this.renderSuggestions(fontRenderer, CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor, Commander.suggestionsFollowParameters);
        }
    }

    private void renderSuggestions(FontRenderer fontRenderer, String message, int cursor, boolean renderAtParameter) {
        int mouseX = GuiHelper.getScaledMouseX(((GuiScreenAccessor)((GuiChat)(Object)this)).mc());
        int mouseY = GuiHelper.getScaledMouseY(((GuiScreenAccessor)((GuiChat)(Object)this)).mc());

        int parameterInCursor = CommandParameterParser.getParameterInCursorIndex(message, cursor);

        int leftMargin = 2;
        if (renderAtParameter)
            leftMargin += CommanderHelper.getLeftMarginForSuggestionsWithParameterIndex(fontRenderer, message, parameterInCursor) + 1;

        int largestSuggestion = 0;
        for (String suggestion : CommanderGuiManager.suggestions)
            if (fontRenderer.getStringWidth(suggestion) > largestSuggestion) largestSuggestion = fontRenderer.getStringWidth(suggestion);

        ((GuiChat)(Object)this).drawRect(leftMargin, ((GuiChat) (Object) this).height - 15 - (Math.min(CommanderGuiManager.suggestions.size(), Commander.maxSuggestions) * 12), largestSuggestion + leftMargin + 1, ((GuiChat) (Object) this).height - 15, Integer.MIN_VALUE);
        if (CommanderGuiManager.scroll < CommanderGuiManager.suggestions.size() - Commander.maxSuggestions) GuiHelper.drawDottedRect(((GuiChat)(Object)this), leftMargin, ((GuiChat) (Object) this).height - 15, largestSuggestion + leftMargin + 1, ((GuiChat) (Object) this).height - 14, Color.WHITE.getRGB(), 1);
        if (CommanderGuiManager.scroll != 0) GuiHelper.drawDottedRect(((GuiChat)(Object)this), leftMargin, ((GuiChat) (Object) this).height - 16 - (Commander.maxSuggestions * 12), largestSuggestion + leftMargin + 1, ((GuiChat) (Object) this).height - 15 - (Commander.maxSuggestions * 12), Color.WHITE.getRGB(), 1);

        if (!CommanderGuiManager.suggestions.isEmpty() && CommanderGuiManager.commandIndex == -1) fontRenderer.drawStringWithShadow("§8" + CommanderGuiManager.suggestions.get(0), leftMargin + 1, ((GuiChat) (Object) this).height - 12, 0xE0E0E0);

        for (int i = 0; i < Math.min(CommanderGuiManager.suggestions.size(), Commander.maxSuggestions); i++) {
            String suggestion = CommanderGuiManager.suggestions.get(i + CommanderGuiManager.scroll);
            int height = 12 * (-i + Math.min(CommanderGuiManager.suggestions.size(), Commander.maxSuggestions) - 1) + 25;
            String colorCode;
            if (i + CommanderGuiManager.scroll == CommanderGuiManager.commandIndex || i + CommanderGuiManager.scroll == this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY)) {
                colorCode = "§4";
            } else {
                colorCode = "§0";
                suggestion = CommanderHelper.addToIndex(suggestion, "§8", CommanderHelper.getCommandParameterListWithoutSlash(CommanderGuiManager.tablessMessage).get(parameterInCursor).length());
            }
            fontRenderer.drawStringWithShadow(colorCode + suggestion, leftMargin + 1, ((GuiChat) (Object) this).height - height, 0xE0E0E0);
        }
    }

    private void renderSingleSuggestionLine(FontRenderer fontRenderer, String text) {
        int leftMargin = 2;
        int stringWidth = fontRenderer.getStringWidth(text);

        ((GuiChat)(Object)this).drawRect(leftMargin, ((GuiChat) (Object) this).height - 27, stringWidth + leftMargin + 1, ((GuiChat) (Object) this).height - 15, Integer.MIN_VALUE);
        fontRenderer.drawStringWithShadow(text, leftMargin + 1, ((GuiChat) (Object) this).height - 25, 0xE0E0E0);
    }

    @Inject(method = "keyTyped", at = @At("RETURN"))
    public void keyTyped(char c, int key, int mouseX, int mouseY, CallbackInfo ci) {
        String parameterInCursor;
        int parameterInCursorIndex;
        if (key != 15 && (Character.isISOControl(c) || ((GuiChat) (Object) this).isCharacterAllowed(c))) {
            this.resetAllManagerVariables();
            parameterInCursor = CommandParameterParser.getParameterInCursor(CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor);
            parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor);
            if (CommanderGuiManager.tablessMessage.startsWith("/")) {
                if (parameterInCursorIndex == 0) {
                    CommanderGuiManager.suggestions = CommandSuggester.getSuggestedCommands(CommanderGuiManager.tablessMessage);
                    if (CommanderGuiManager.suggestions.isEmpty()) {
                        CommanderGuiManager.currentError = I18n.getInstance().translateKey("commands.commander.no_commands_available");
                    }
                } else {
                    CommanderGuiManager.suggestions = CommandSuggester.getCommandSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), parameterInCursorIndex, parameterInCursor, Commands.getCommand(CommanderHelper.getCommandParameterListWithoutSlash(CommanderGuiManager.tablessMessage).get(0)));
                }
            } else {
                CommanderGuiManager.suggestions = new ArrayList<>();
            }
        } else if (key == 15) {
            ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor().setText(CommanderGuiManager.tablessMessage);
            ((TextFieldEditorAccessor)((GuiChat)(Object)this)).editor().setCursor(CommanderGuiManager.cursor);

            parameterInCursor = CommandParameterParser.getParameterInCursor(CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor);
            parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor);
            if (!CommanderGuiManager.tablessMessage.startsWith("/")) {
                CommanderGuiManager.suggestions = CommandSuggester.getDefaultSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), parameterInCursor);
                if (!CommanderGuiManager.suggestions.isEmpty()) {
                    this.cycleThroughSuggestions(parameterInCursorIndex);
                }
            } else {
                if (parameterInCursorIndex == 0) {
                    if (!CommanderGuiManager.suggestions.isEmpty()) {
                        this.cycleThroughSuggestions(parameterInCursorIndex, "/");
                    } else {
                        CommanderGuiManager.currentError = I18n.getInstance().translateKey("commands.commander.no_commands_available");
                    }
                } else {
                    CommanderGuiManager.suggestions = CommandSuggester.getCommandSuggestions(((GuiScreenAccessor)((GuiChat)(Object)this)).mc(), parameterInCursorIndex, parameterInCursor, Commands.getCommand(CommanderHelper.getCommandParameterListWithoutSlash(CommanderGuiManager.tablessMessage).get(0)));
                    if (!CommanderGuiManager.suggestions.isEmpty()) {
                        this.cycleThroughSuggestions(parameterInCursorIndex);
                    }
                }
            }
        }
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    public void onGuiClosed(CallbackInfo ci) {
        CommanderGuiManager.commandIndex = -1;
        CommanderGuiManager.tablessMessage = "";
        CommanderGuiManager.cursor = -1;
        CommanderGuiManager.currentError = "";
        CommanderGuiManager.suggestions = new ArrayList<>();
        CommanderGuiManager.scroll = 0;
    }

    @Inject(method = "initGui", at = @At("HEAD"))
    public void initGui(CallbackInfo ci) {
        this.resetAllManagerVariables();
    }

    @Inject(method = "updateScreen", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void updateScreen(CallbackInfo ci, int dWheel) {
        int cursorX = GuiHelper.getScaledMouseX(((GuiScreenAccessor)((GuiChat)(Object)this)).mc());
        int cursorY = GuiHelper.getScaledMouseY(((GuiScreenAccessor)((GuiChat)(Object)this)).mc());
        if (isHoveringOverSuggestions(cursorX, cursorY) && dWheel != 0) {
            CommanderGuiManager.scroll(Math.round(Math.signum(dWheel)) * -1);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void mouseClicked(int x, int y, int button, CallbackInfo ci) {
        if (isHoveringOverSuggestions(x, y)) {
            int parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor);
            String prefix = CommanderGuiManager.tablessMessage.startsWith("/") && parameterInCursorIndex == 0 ? "/" : "";
            this.cycleToSuggestion(this.getIndexOfSuggestionBeingHoveredOver(x, y), parameterInCursorIndex, prefix);
        }
    }

    private boolean isHoveringOverSuggestions(int cursorX, int cursorY) {
        return this.getIndexOfSuggestionBeingHoveredOver(cursorX, cursorY) != -1;
    }

    private int getIndexOfSuggestionBeingHoveredOver(int cursorX, int cursorY) {
        int parameterInCursor = CommandParameterParser.getParameterInCursorIndex(CommanderGuiManager.tablessMessage, CommanderGuiManager.cursor);
        FontRenderer fontRenderer = ((GuiScreenAccessor)((GuiChat)(Object)this)).fontRenderer();

        int minX = 2;
        if (Commander.suggestionsFollowParameters)
            minX += CommanderHelper.getLeftMarginForSuggestionsWithParameterIndex(fontRenderer, CommanderGuiManager.tablessMessage, parameterInCursor) + 1;

        int largestSuggestion = 0;
        for (String suggestion : CommanderGuiManager.suggestions)
            if (fontRenderer.getStringWidth(suggestion) > largestSuggestion) largestSuggestion = fontRenderer.getStringWidth(suggestion);

        int maxX = largestSuggestion + minX + 1;
        for (int i = 0; i < Math.min(CommanderGuiManager.suggestions.size(), Commander.maxSuggestions); i++) {
            int minY = ((GuiChat) (Object) this).height - 14 - ((Math.min(CommanderGuiManager.suggestions.size(), Commander.maxSuggestions) - i) * 12);
            int maxY = ((GuiChat) (Object) this).height - 2 - ((Math.min(CommanderGuiManager.suggestions.size(), Commander.maxSuggestions) - i) * 12);

            if (cursorX >= minX && cursorX < maxX && cursorY >= minY && cursorY < maxY) {
                return i + CommanderGuiManager.scroll;
            }
        }
        return -1;
    }

    private void resetAllManagerVariables() {
        CommanderGuiManager.commandIndex = -1;
        CommanderGuiManager.tablessMessage = ((GuiChat) (Object) this).getText();
        CommanderGuiManager.cursor = ((TextFieldEditorAccessor) ((GuiChat) (Object) this)).editor().getCursor();
        CommanderGuiManager.currentError = "";
        CommanderGuiManager.suggestions = new ArrayList<>();
        CommanderGuiManager.scroll = 0;
    }

    private void cycleThroughSuggestions(int parameterInCursorIndex) {
        this.cycleThroughSuggestions(parameterInCursorIndex, "");
    }

    private void cycleThroughSuggestions(int parameterInCursorIndex, String parameterPrefix) {
        this.cycleToSuggestion(CommanderGuiManager.commandIndex + 1, parameterInCursorIndex, parameterPrefix);
    }

    private void cycleToSuggestion(int suggestionIndex, int parameterInCursorIndex) {
        this.cycleToSuggestion(suggestionIndex, parameterInCursorIndex, "");
    }
    
    private void cycleToSuggestion(int suggestionIndex, int parameterInCursorIndex, String parameterPrefix) {
        CommanderGuiManager.commandIndex = suggestionIndex % CommanderGuiManager.suggestions.size();
        String newString = CommandParameterParser.replaceParameterOnString(CommanderGuiManager.tablessMessage, parameterInCursorIndex, parameterPrefix + CommanderGuiManager.suggestions.get(CommanderGuiManager.commandIndex));
        String parameterInCursor = "";
        if (!CommanderHelper.getCommandParameterList(CommanderGuiManager.tablessMessage).isEmpty()) {
            parameterInCursor = CommanderHelper.getCommandParameterList(CommanderGuiManager.tablessMessage).get(parameterInCursorIndex);
        }
        ((TextFieldEditorAccessor) ((GuiChat) (Object) this)).editor().setText(newString);
        ((TextFieldEditorAccessor) ((GuiChat) (Object) this)).editor().setCursor(CommandParameterParser.getCharIndexInEndOfParameterOnString(newString, parameterInCursorIndex) - parameterInCursor.length() + parameterPrefix.length() + CommanderGuiManager.suggestions.get(CommanderGuiManager.commandIndex).length());

        if (CommanderGuiManager.commandIndex < CommanderGuiManager.scroll) {
            CommanderGuiManager.scroll = CommanderGuiManager.commandIndex;
        } else if (CommanderGuiManager.scroll + Commander.maxSuggestions - 1 < CommanderGuiManager.commandIndex) {
            CommanderGuiManager.scroll = CommanderGuiManager.commandIndex - (Commander.maxSuggestions - 1);
        }
    }
}
