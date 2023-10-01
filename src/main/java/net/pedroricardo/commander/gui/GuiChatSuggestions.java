package net.pedroricardo.commander.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTooltip;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.world.WorldServer;
import net.pedroricardo.commander.*;
import net.pedroricardo.commander.content.CommanderClientCommandSource;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.duck.EnvironmentWithManager;
import net.pedroricardo.commander.mixin.ServerFromWorldAccessor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OptionalGetWithoutIsPresent")
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
    GuiTooltip tooltip;
    
    public GuiChatSuggestions(Minecraft mc, TextFieldEditor textFieldEditor, GuiChat chat) {
        this.mc = mc;
        this.fontRenderer = this.mc.fontRenderer;
        this.commandSource = new CommanderClientCommandSource(this.mc);
        this.editor = textFieldEditor;
        this.chat = chat;
        this.tablessMessage = this.chat.getText();
        this.tablessCursor = this.editor.getCursor();
        this.tooltip = new GuiTooltip(this.mc);
    }

    public CommanderCommandManager getManager() {
        if (this.mc.theWorld instanceof WorldServer) {
            return ((EnvironmentWithManager)((ServerFromWorldAccessor)((WorldServer)this.mc.theWorld)).mcServer()).getManager();
        }
        return ((EnvironmentWithManager)this.mc).getManager();
    }

    public void drawScreen() {
        CommandSyntaxException parseException;
        if (!this.suggestions.isEmpty()) {
            this.renderSuggestions(this.fontRenderer, this.tablessMessage);
        } else if (this.parseResults != null) {
            if (!this.parseResults.getExceptions().isEmpty()) {
                int i = 0;
                for (Exception e : this.parseResults.getExceptions().values()) {
                    this.renderSingleSuggestionLine(this.mc.fontRenderer, TextFormatting.RED + e.getMessage(), i, false);
                    i++;
                }
            } else if ((parseException = CommanderCommandManager.getParseException(this.parseResults)) != null) {
                this.renderSingleSuggestionLine(this.mc.fontRenderer, TextFormatting.RED + parseException.getMessage(), 0, false);
            } else {
                List<String> commandUsage = getCommandUsage(this.tablessCursor);
                for (int i = 0; i < commandUsage.size(); i++) {
                    this.renderSingleSuggestionLine(this.mc.fontRenderer, TextFormatting.LIGHT_GRAY + commandUsage.get(i), i, true);
                }
            }
        }
    }

    private void renderSuggestions(FontRenderer fontRenderer, String message) {
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
            if (i + this.scroll == this.commandIndex || (this.isHoveringOverSuggestions(mouseX, mouseY) && i + this.scroll == this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get())) {
                colorCode = TextFormatting.YELLOW.toString();
            } else {
                colorCode = TextFormatting.LIGHT_GRAY.toString();
            }
            fontRenderer.drawStringWithShadow(colorCode + suggestionText, leftMargin + 1, height - suggestionHeight, 0xE0E0E0);
        }

        if (this.isHoveringOverSuggestions(mouseX, mouseY) && this.suggestions.get(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get()).getTooltip() != null) {
            this.tooltip.render(this.suggestions.get(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get()).getTooltip().getString(), mouseX, mouseY, 0, 0);
        }
    }

    private void renderSingleSuggestionLine(FontRenderer fontRenderer, String text, int heightIndex, boolean followParameters) {
        int height = this.mc.resolution.scaledHeight - heightIndex * 12;
        int leftMargin = 2;
        int stringWidth = fontRenderer.getStringWidth(text);

        if (Commander.suggestionsFollowParameters && this.parseResults != null && followParameters)
            leftMargin += fontRenderer.getStringWidth(this.tablessMessage.substring(0, this.parseResults.getContext().findSuggestionContext(this.tablessCursor).startPos)) + 1;

        this.drawRect(leftMargin, height - 27, stringWidth + leftMargin + 1, height - 15, Integer.MIN_VALUE);
        fontRenderer.drawStringWithShadow(text, leftMargin + 1, height - 25, 0xE0E0E0);
    }

    private List<String> getCommandUsage(int cursor) {
        List<String> commandUsage = new ArrayList<>();
        if (this.parseResults == null || this.parseResults.getContext().getRootNode() == null || this.parseResults.getContext().getRange().getStart() > cursor) return commandUsage;
        for (Map.Entry<CommandNode<CommanderCommandSource>, String> entry : this.getManager().getDispatcher().getSmartUsage(this.parseResults.getContext().findSuggestionContext(cursor).parent, this.commandSource).entrySet()) {
            if (entry.getKey() instanceof LiteralCommandNode) continue;
            commandUsage.add(entry.getValue());
        }
        return commandUsage;
    }

    public void keyTyped(char c, int key) {
        int mouseX = GuiHelper.getScaledMouseX(this.mc);
        int mouseY = GuiHelper.getScaledMouseY(this.mc) - 1;

        if (key == 15) {
            if (this.commandIndex == -1 && this.isHoveringOverSuggestions(mouseX, mouseY)) {
                this.cycleToSuggestion(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get());
            } else if (Keyboard.isKeyDown(42)) {
                this.cycleThroughSuggestions(-1);
            } else {
                this.cycleThroughSuggestions();
            }
            return;
        }

        if (key == 42) return;

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
            CommandDispatcher<CommanderCommandSource> dispatcher = this.getManager().getDispatcher();
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
    }

    private void updateSuggestions() {
        this.suggestions = new ArrayList<>();
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            Suggestions suggestions = this.pendingSuggestions.join();
            this.suggestions.addAll(suggestions.getList());
        }
    }

    public void updateScreen(int dWheel) {
        int cursorX = GuiHelper.getScaledMouseX(this.mc);
        int cursorY = GuiHelper.getScaledMouseY(this.mc);
        if (this.isHoveringOverSuggestions(cursorX, cursorY) && dWheel != 0) {
            this.scroll(Math.round(Math.signum(dWheel)) * -1);
        }
    }

    public void mouseClicked(int x, int y, int button) {
        if (isHoveringOverSuggestions(x, y) && button == 0) {
            this.cycleToSuggestion(this.getIndexOfSuggestionBeingHoveredOver(x, y).get());
        }
    }

    public boolean isHoveringOverSuggestions(int cursorX, int cursorY) {
        return this.getIndexOfSuggestionBeingHoveredOver(cursorX, cursorY).isPresent();
    }

    public Optional<Integer> getIndexOfSuggestionBeingHoveredOver(int cursorX, int cursorY) {
        if (this.suggestions.size() == 0) return Optional.empty();
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
                return Optional.of(i + this.scroll);
            }
        }
        return Optional.empty();
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
        this.cycleThroughSuggestions(1);
    }

    public void cycleThroughSuggestions(int amount) {
        this.cycleToSuggestion(this.commandIndex + amount);
    }

    public void cycleToSuggestion(int index) {
        if (this.suggestions.size() == 0) return;
        if (index < 0) index += this.suggestions.size();
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

    public CommanderCommandSource getCommandSource() {
        return this.commandSource;
    }
}
