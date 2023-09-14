package net.pedroricardo.commander.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.Commands;
import net.pedroricardo.commander.*;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class GuiChatSuggestions extends Gui {
    private final TextFieldEditor editor;
    private final GuiChat chat;
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    private int commandIndex = -1;
    private String tablessMessage;
    private int tablessCursor;
    private String currentError = "";
    private List<String> suggestions = new ArrayList<>();
    private int scroll = 0;
    
    public GuiChatSuggestions(Minecraft mc, TextFieldEditor textFieldEditor, GuiChat chat) {
        this.mc = mc;
        this.fontRenderer = this.mc.fontRenderer;
        this.editor = textFieldEditor;
        this.chat = chat;
        this.tablessMessage = this.chat.getText();
        this.tablessCursor = this.editor.getCursor();
    }

    public void drawScreen() {
        if (!this.currentError.isEmpty()) {
            this.renderSingleSuggestionLine(this.mc.fontRenderer, "§e" + this.currentError);
        } else if (!this.suggestions.isEmpty()) {
            this.renderSuggestions(this.fontRenderer, this.tablessMessage, this.tablessCursor);
        }
    }

    private void renderSuggestions(FontRenderer fontRenderer, String message, int cursor) {
        int height = this.mc.resolution.scaledHeight;
        int mouseX = GuiHelper.getScaledMouseX(this.mc);
        int mouseY = GuiHelper.getScaledMouseY(this.mc);

        int parameterInCursor = CommandParameterParser.getParameterInCursorIndex(message, cursor);

        int leftMargin = 2;
        if (Commander.suggestionsFollowParameters)
            leftMargin += CommanderHelper.getLeftMarginForSuggestionsWithParameterIndex(fontRenderer, message, parameterInCursor) + 1;

        int largestSuggestion = 0;
        for (String suggestion : this.suggestions)
            if (fontRenderer.getStringWidth(suggestion) > largestSuggestion) largestSuggestion = fontRenderer.getStringWidth(suggestion);

        this.drawRect(leftMargin, height - 15 - (Math.min(this.suggestions.size(), Commander.maxSuggestions) * 12), largestSuggestion + leftMargin + 1, height - 15, Integer.MIN_VALUE);
        if (this.scroll < this.suggestions.size() - Commander.maxSuggestions) GuiHelper.drawDottedRect(this, leftMargin, height - 15, largestSuggestion + leftMargin + 1, height - 14, Color.WHITE.getRGB(), 1);
        if (this.scroll != 0) GuiHelper.drawDottedRect(this, leftMargin, height - 16 - (Commander.maxSuggestions * 12), largestSuggestion + leftMargin + 1, height - 15 - (Commander.maxSuggestions * 12), Color.WHITE.getRGB(), 1);

        for (int i = 0; i < Math.min(this.suggestions.size(), Commander.maxSuggestions); i++) {
            String suggestion = this.suggestions.get(i + this.scroll);
            int suggestionHeight = 12 * (-i + Math.min(this.suggestions.size(), Commander.maxSuggestions) - 1) + 25;
            String colorCode;
            if (i + this.scroll == this.commandIndex || i + this.scroll == this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY)) {
                colorCode = "§4";
            } else {
                colorCode = "§0";
                suggestion = CommanderHelper.addToIndex(suggestion, "§8", CommanderHelper.getCommandParameterListWithoutSlash(this.tablessMessage).get(parameterInCursor).length());
            }
            fontRenderer.drawStringWithShadow(colorCode + suggestion, leftMargin + 1, height - suggestionHeight, 0xE0E0E0);
        }
    }

    private void renderSingleSuggestionLine(FontRenderer fontRenderer, String text) {
        int height = this.mc.resolution.scaledHeight;
        int leftMargin = 2;
        int stringWidth = fontRenderer.getStringWidth(text);

        this.drawRect(leftMargin, height - 27, stringWidth + leftMargin + 1, height - 15, Integer.MIN_VALUE);
        fontRenderer.drawStringWithShadow(text, leftMargin + 1, height - 25, 0xE0E0E0);
    }

    public void keyTyped(char c, int key) {
        String parameterInCursor;
        int parameterInCursorIndex;
        if (key != 15 && (Character.isISOControl(c) || this.chat.isCharacterAllowed(c))) {
            this.resetAllManagerVariables();
            parameterInCursor = CommandParameterParser.getParameterInCursor(this.tablessMessage, this.tablessCursor);
            parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(this.tablessMessage, this.tablessCursor);
            if (this.tablessMessage.startsWith("/")) {
                if (parameterInCursorIndex == 0) {
                    this.suggestions = CommandSuggester.getSuggestedCommands(this.tablessMessage);
                    if (this.suggestions.isEmpty()) {
                        this.currentError = I18n.getInstance().translateKey("commands.commander.no_commands_available");
                    }
                } else {
                    this.suggestions = CommandSuggester.getCommandSuggestions(this.mc, parameterInCursorIndex, parameterInCursor, Commands.getCommand(CommanderHelper.getCommandParameterListWithoutSlash(this.tablessMessage).get(0)));
                }
            } else {
                this.suggestions = new ArrayList<>();
            }
        } else if (key == 15) {
            this.editor.setText(this.tablessMessage);
            this.editor.setCursor(this.tablessCursor);

            parameterInCursor = CommandParameterParser.getParameterInCursor(this.tablessMessage, this.tablessCursor);
            parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(this.tablessMessage, this.tablessCursor);
            if (!this.tablessMessage.startsWith("/")) {
                this.suggestions = CommandSuggester.getDefaultSuggestions(this.mc, parameterInCursor);
                if (!this.suggestions.isEmpty()) {
                    this.cycleThroughSuggestions(parameterInCursorIndex);
                }
            } else {
                if (parameterInCursorIndex == 0) {
                    if (!this.suggestions.isEmpty()) {
                        this.cycleThroughSuggestions(parameterInCursorIndex, "/");
                    } else {
                        this.currentError = I18n.getInstance().translateKey("commands.commander.no_commands_available");
                    }
                } else {
                    this.suggestions = CommandSuggester.getCommandSuggestions(this.mc, parameterInCursorIndex, parameterInCursor, Commands.getCommand(CommanderHelper.getCommandParameterListWithoutSlash(this.tablessMessage).get(0)));
                    if (!this.suggestions.isEmpty()) {
                        this.cycleThroughSuggestions(parameterInCursorIndex);
                    }
                }
            }
        }
    }

    public void updateScreen(int dWheel) {
        int cursorX = GuiHelper.getScaledMouseX(this.mc);
        int cursorY = GuiHelper.getScaledMouseY(this.mc);
        if (isHoveringOverSuggestions(cursorX, cursorY) && dWheel != 0) {
            this.scroll(Math.round(Math.signum(dWheel)) * -1);
        }
    }

    public void mouseClicked(int x, int y, int button) {
        if (isHoveringOverSuggestions(x, y) && button == 0) {
            int parameterInCursorIndex = CommandParameterParser.getParameterInCursorIndex(this.tablessMessage, this.tablessCursor);
            String prefix = this.tablessMessage.startsWith("/") && parameterInCursorIndex == 0 ? "/" : "";
            this.cycleToSuggestion(this.getIndexOfSuggestionBeingHoveredOver(x, y), parameterInCursorIndex, prefix);
        }
    }

    private boolean isHoveringOverSuggestions(int cursorX, int cursorY) {
        return this.getIndexOfSuggestionBeingHoveredOver(cursorX, cursorY) != -1;
    }

    private int getIndexOfSuggestionBeingHoveredOver(int cursorX, int cursorY) {
        int height = this.mc.resolution.scaledHeight;
        int parameterInCursor = CommandParameterParser.getParameterInCursorIndex(this.tablessMessage, this.tablessCursor);

        int minX = 2;
        if (Commander.suggestionsFollowParameters)
            minX += CommanderHelper.getLeftMarginForSuggestionsWithParameterIndex(this.fontRenderer, this.tablessMessage, parameterInCursor) + 1;

        int largestSuggestion = 0;
        for (String suggestion : this.suggestions)
            if (this.fontRenderer.getStringWidth(suggestion) > largestSuggestion) largestSuggestion = this.fontRenderer.getStringWidth(suggestion);

        int maxX = largestSuggestion + minX + 1;
        for (int i = 0; i < Math.min(this.suggestions.size(), Commander.maxSuggestions); i++) {
            int minY = height - 14 - ((Math.min(this.suggestions.size(), Commander.maxSuggestions) - i) * 12);
            int maxY = height - 2 - ((Math.min(this.suggestions.size(), Commander.maxSuggestions) - i) * 12);

            if (cursorX >= minX && cursorX < maxX && cursorY >= minY && cursorY < maxY) {
                return i + this.scroll;
            }
        }
        return -1;
    }

    private void resetAllManagerVariables() {
        this.commandIndex = -1;
        this.tablessMessage = this.chat.getText();
        this.tablessCursor = this.editor.getCursor();
        this.currentError = "";
        this.suggestions = new ArrayList<>();
        this.scroll = 0;
    }

    private void cycleThroughSuggestions(int parameterInCursorIndex) {
        this.cycleThroughSuggestions(parameterInCursorIndex, "");
    }

    private void cycleThroughSuggestions(int parameterInCursorIndex, String parameterPrefix) {
        this.cycleToSuggestion(this.commandIndex + 1, parameterInCursorIndex, parameterPrefix);
    }

    private void cycleToSuggestion(int suggestionIndex, int parameterInCursorIndex) {
        this.cycleToSuggestion(suggestionIndex, parameterInCursorIndex, "");
    }

    private void cycleToSuggestion(int suggestionIndex, int parameterInCursorIndex, String parameterPrefix) {
        this.commandIndex = suggestionIndex % this.suggestions.size();
        String newString = CommandParameterParser.replaceParameterOnString(this.tablessMessage, parameterInCursorIndex, parameterPrefix + this.suggestions.get(this.commandIndex));
        String parameterInCursor = "";
        if (!CommanderHelper.getCommandParameterList(newString).isEmpty()) {
            parameterInCursor = CommanderHelper.getCommandParameterList(newString).get(parameterInCursorIndex);
        }
        this.editor.setText(newString);
        this.editor.setCursor(CommandParameterParser.getCharIndexInEndOfParameterOnString(newString, parameterInCursorIndex) - parameterInCursor.length() + parameterPrefix.length() + this.suggestions.get(this.commandIndex).length());

        if (this.commandIndex < this.scroll) {
            this.scroll = this.commandIndex;
        } else if (this.scroll + Commander.maxSuggestions - 1 < this.commandIndex) {
            this.scroll = this.commandIndex - (Commander.maxSuggestions - 1);
        }
    }

    public boolean scroll(int amount) {
        if (this.scroll + amount >= 0 && this.scroll + amount <= this.suggestions.size() - Commander.maxSuggestions) {
            this.scroll += amount;
            return true;
        }
        return false;
    }

    public List<String> getSuggestions() {
        return new ArrayList<>(this.suggestions);
    }

    public int getScroll() {
        return this.scroll;
    }

    public int getCommandIndex() {
        return this.commandIndex;
    }

    public String getMessage() {
        return this.tablessMessage;
    }

    public int getCursor() {
        return this.tablessCursor;
    }
}
