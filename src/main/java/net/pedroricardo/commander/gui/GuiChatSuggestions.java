package net.pedroricardo.commander.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.pedroricardo.commander.*;
import net.pedroricardo.commander.commands.CommanderClientCommandSource;
import net.pedroricardo.commander.commands.CommanderCommandManager;
import net.pedroricardo.commander.commands.CommanderCommandSource;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class GuiChatSuggestions extends Gui {
    private final TextFieldEditor editor;
    private final GuiChat chat;
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    private final CommanderClientCommandSource commandSource;
    @Nullable
    private ParseResults<CommanderCommandSource> parseResults;
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    private int commandIndex = -1;
    private String tablessMessage;
    private int tablessCursor;
    private List<Suggestion> suggestions = new ArrayList<>();
    private int scroll = 0;
    
    public GuiChatSuggestions(Minecraft mc, TextFieldEditor textFieldEditor, GuiChat chat) {
        this.mc = mc;
        this.fontRenderer = this.mc.fontRenderer;
        this.commandSource = new CommanderClientCommandSource(this.mc);
        this.editor = textFieldEditor;
        this.chat = chat;
        this.tablessMessage = this.chat.getText();
        this.tablessCursor = this.editor.getCursor();
    }

    public void drawScreen() {
        if (!this.suggestions.isEmpty()) {
            this.renderSuggestions(this.fontRenderer, this.tablessMessage, this.tablessCursor);
        } else if (this.parseResults != null && !this.parseResults.getExceptions().isEmpty()) {
            for (Exception e : this.parseResults.getExceptions().values()) {
                this.renderSingleSuggestionLine(this.mc.fontRenderer, "§e" + e.getMessage());
            }
        }
    }

    private void renderSuggestions(FontRenderer fontRenderer, String message, int cursor) {
        int height = this.mc.resolution.scaledHeight;
        int mouseX = GuiHelper.getScaledMouseX(this.mc);
        int mouseY = GuiHelper.getScaledMouseY(this.mc) - 1;

        int parameterStart = this.suggestions.get(0).getRange().getStart();

        int leftMargin = 2;
        if (Commander.suggestionsFollowParameters)
            leftMargin += fontRenderer.getStringWidth(message.substring(0, parameterStart)) + 1;

        int largestSuggestion = 0;
        for (Suggestion suggestion : this.suggestions)
            if (fontRenderer.getStringWidth(suggestion.getText()) > largestSuggestion) largestSuggestion = fontRenderer.getStringWidth(suggestion.getText());

        this.drawRect(leftMargin, height - 15 - (Math.min(this.suggestions.size(), Commander.maxSuggestions) * 12), largestSuggestion + leftMargin + 1, height - 15, Integer.MIN_VALUE);
        if (this.scroll < this.suggestions.size() - Commander.maxSuggestions) GuiHelper.drawDottedRect(this, leftMargin, height - 15, largestSuggestion + leftMargin + 1, height - 14, Color.WHITE.getRGB(), 1);
        if (this.scroll != 0) GuiHelper.drawDottedRect(this, leftMargin, height - 16 - (Commander.maxSuggestions * 12), largestSuggestion + leftMargin + 1, height - 15 - (Commander.maxSuggestions * 12), Color.WHITE.getRGB(), 1);

        for (int i = 0; i < Math.min(this.suggestions.size(), Commander.maxSuggestions); i++) {
            String suggestionText = this.suggestions.get(i + this.scroll).getText();
            int suggestionHeight = 12 * (-i + Math.min(this.suggestions.size(), Commander.maxSuggestions) - 1) + 25;
            String colorCode;
            if (i + this.scroll == this.commandIndex || i + this.scroll == this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY)) {
                colorCode = "§4";
            } else {
                colorCode = "§8";
            }
            fontRenderer.drawStringWithShadow(colorCode + suggestionText, leftMargin + 1, height - suggestionHeight, 0xE0E0E0);
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
        if (key != 15) {
            this.resetAllManagerVariables();
            String text = this.editor.getText();
            int cursor = this.editor.getCursor();
            if (this.parseResults != null && !this.parseResults.getReader().getString().equals(text)) {
                this.parseResults = null;
            }

            StringReader stringReader = new StringReader(text);
            boolean bl = stringReader.canRead() && stringReader.peek() == '/';
            if (bl) {
                stringReader.skip();
                CommandDispatcher<CommanderCommandSource> dispatcher = CommanderCommandManager.getDispatcher();
                if (this.parseResults == null) {
                    this.parseResults = dispatcher.parse(stringReader, this.commandSource);
                }
                if (cursor >= 1) {
                    this.pendingSuggestions = dispatcher.getCompletionSuggestions(this.parseResults, cursor);
                    this.pendingSuggestions.thenRun(() -> {
                        if (this.pendingSuggestions.isDone()) {
                            this.updateSuggestions();
                        }
                    });
                }
            }
        } else {
            this.cycleThroughSuggestions();
        }
    }

    private void updateSuggestions() {
        this.suggestions = new ArrayList<>();
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            Suggestions suggestions = this.pendingSuggestions.join();
            this.suggestions.addAll(suggestions.getList());
            this.suggestions.addAll(CommanderHelper.getLegacySuggestionList(this.tablessMessage, this.tablessCursor));
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
            this.cycleToSuggestion(this.getIndexOfSuggestionBeingHoveredOver(x, y));
        }
    }

    public boolean isHoveringOverSuggestions(int cursorX, int cursorY) {
        return this.getIndexOfSuggestionBeingHoveredOver(cursorX, cursorY) != -1;
    }

    public int getIndexOfSuggestionBeingHoveredOver(int cursorX, int cursorY) {
        if (this.suggestions.size() == 0) return -1;
        int height = this.mc.resolution.scaledHeight;

        int parameterStart = this.suggestions.get(0).getRange().getStart();

        int minX = 2;
        if (Commander.suggestionsFollowParameters)
            minX += fontRenderer.getStringWidth(this.tablessMessage.substring(0, parameterStart)) + 1;

        int largestSuggestion = 0;
        for (Suggestion suggestion : this.suggestions)
            if (this.fontRenderer.getStringWidth(suggestion.getText()) > largestSuggestion) largestSuggestion = this.fontRenderer.getStringWidth(suggestion.getText());

        int maxX = largestSuggestion + minX + 1;
        for (int i = 0; i < Math.min(this.suggestions.size(), Commander.maxSuggestions); i++) {
            int minY = height - 15 - ((Math.min(this.suggestions.size(), Commander.maxSuggestions) - i) * 12);
            int maxY = height - 3 - ((Math.min(this.suggestions.size(), Commander.maxSuggestions) - i) * 12);

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
        this.suggestions = new ArrayList<>();
        this.scroll = 0;
    }

    public void scroll(int amount) {
        if (this.scroll + amount >= 0 && this.scroll + amount <= this.suggestions.size() - Commander.maxSuggestions) {
            this.scroll += amount;
        }
    }

    public void cycleThroughSuggestions() {
        this.cycleToSuggestion(this.commandIndex + 1);
    }

    public void cycleToSuggestion(int index) {
        if (this.suggestions.size() == 0) return;
        this.commandIndex = index % this.suggestions.size();
        Suggestion suggestion = this.suggestions.get(this.commandIndex);
        this.editor.setText(suggestion.apply(this.tablessMessage));
        this.editor.setCursor(suggestion.getRange().getStart() + suggestion.getText().length());
        if (this.commandIndex >= this.scroll + Commander.maxSuggestions) {
            this.scroll = this.commandIndex - Commander.maxSuggestions + 1;
        } else if (this.commandIndex < this.scroll) {
            this.scroll = this.commandIndex;
        }
    }

    public List<Suggestion> getSuggestions() {
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

    public @Nullable ParseResults<CommanderCommandSource> getParseResults() {
        return this.parseResults;
    }
}
