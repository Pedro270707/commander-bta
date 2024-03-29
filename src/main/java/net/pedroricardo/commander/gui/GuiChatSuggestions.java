package net.pedroricardo.commander.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTooltip;
import net.minecraft.client.gui.hud.ComponentAnchor;
import net.minecraft.client.gui.text.ITextField;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.FontRenderer;
import net.minecraft.core.net.command.TextFormatting;
import net.pedroricardo.commander.*;
import net.pedroricardo.commander.content.*;
import net.pedroricardo.commander.duck.ClassWithManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Unique;
import turniplabs.halplibe.helper.ModVersionHelper;
import turniplabs.halplibe.util.version.EnumModList;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "unused"})
public class GuiChatSuggestions extends Gui {
    private final TextFieldEditor editor;
    private final ITextField textField;
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    private final CommanderCommandSource commandSource;
    private final PositionSupplier<Integer> xSupplier;
    private final PositionSupplier<Integer> ySupplier;
    private ComponentAnchor anchor;
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

    public boolean hidden = false;

    public GuiChatSuggestions(Minecraft mc, TextFieldEditor textFieldEditor, ITextField textField, PositionSupplier<Integer> xSupplier, PositionSupplier<Integer> ySupplier, ComponentAnchor anchor) {
        this.mc = mc;
        this.xSupplier = xSupplier;
        this.ySupplier = ySupplier;
        this.anchor = anchor;
        this.fontRenderer = this.mc.fontRenderer;
        this.commandSource = new CommanderClientCommandSource(this.mc);
        this.editor = textFieldEditor;
        this.textField = textField;
        this.tablessMessage = this.textField.getText();
        this.tablessCursor = this.editor.getCursor();
        this.tooltip = new GuiTooltip(this.mc);
        Commander.serverSuggestions = new JsonObject();
    }

    public CommanderCommandManager getManager() {
        return this.mc.theWorld == null ? new CommanderCommandManager(false) : ((ClassWithManager)this.mc.theWorld).getManager();
    }

    public void drawScreen() {
        if (this.hidden) return;
        CommandSyntaxException parseException;

        if (!this.suggestions.isEmpty()) {
            this.renderSuggestions(this.fontRenderer, this.tablessMessage, this.suggestions.get(0).getRange().getStart());
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
        } else if (!Commander.serverSuggestions.isEmpty()) {
            if (Commander.serverSuggestions.has(CommandManagerPacketKeys.USAGE) && !Commander.serverSuggestions.getAsJsonArray(CommandManagerPacketKeys.USAGE).isEmpty()) {
                for (int i = 0; i < Commander.serverSuggestions.getAsJsonArray(CommandManagerPacketKeys.USAGE).size(); i++) {
                    this.renderSingleSuggestionLine(this.mc.fontRenderer, TextFormatting.LIGHT_GRAY + Commander.serverSuggestions.getAsJsonArray(CommandManagerPacketKeys.USAGE).get(i).getAsJsonObject().get(CommandManagerPacketKeys.VALUE).getAsString(), i, true);
                }
            } else if (!Commander.serverSuggestions.getAsJsonArray(CommandManagerPacketKeys.EXCEPTIONS).isEmpty()) {
                int i = 0;
                for (JsonElement exception : Commander.serverSuggestions.getAsJsonArray(CommandManagerPacketKeys.EXCEPTIONS)) {
                    this.renderSingleSuggestionLine(this.mc.fontRenderer, TextFormatting.RED + exception.getAsJsonObject().get(CommandManagerPacketKeys.VALUE).getAsString(), i, false);
                    i++;
                }
            }
        }
    }

    private void renderSuggestions(FontRenderer fontRenderer, String message, int start) {
        int mouseX = GuiHelper.getScaledMouseX(this.mc);
        int mouseY = GuiHelper.getScaledMouseY(this.mc) - 1;

        int leftMargin = this.xSupplier.get(this.textField, this, this.mc, Commander.suggestionsFollowParameters);

        int largestSuggestion = 0;
        for (Suggestion suggestion : this.suggestions)
            if (fontRenderer.getStringWidth(suggestion.getText()) > largestSuggestion) largestSuggestion = fontRenderer.getStringWidth(suggestion.getText());

        leftMargin -= (int) (this.anchor.xPosition * (largestSuggestion + 2));

        int minY = this.ySupplier.get(this.textField, this, this.mc, Commander.suggestionsFollowParameters);
        int suggestionBoxHeight = Math.min(this.suggestions.size(), Commander.maxSuggestions) * 12;

        minY += (int) CommanderHelper.linearInterpolation(this.anchor.yPosition, 1, -suggestionBoxHeight - 1);

        this.drawRect(leftMargin, minY, largestSuggestion + leftMargin + 1, minY + suggestionBoxHeight, Integer.MIN_VALUE);
        if (this.scroll < this.suggestions.size() - Commander.maxSuggestions) GuiHelper.drawDottedRect(this, leftMargin, minY + suggestionBoxHeight, largestSuggestion + leftMargin + 1, minY + suggestionBoxHeight + 1, Color.WHITE.getRGB(), 1);
        if (this.scroll != 0) GuiHelper.drawDottedRect(this, leftMargin, minY - 1, largestSuggestion + leftMargin + 1, minY, Color.WHITE.getRGB(), 1);

        for (int i = 0; i < Math.min(this.suggestions.size(), Commander.maxSuggestions); i++) {
            String suggestionText = this.suggestions.get(i + this.scroll).getText();
            int suggestionHeight = 12 * (-i + Math.min(this.suggestions.size(), Commander.maxSuggestions) - 2) + 25;
            String colorCode;
            if (i + this.scroll == this.commandIndex || (this.isHoveringOverSuggestions(mouseX, mouseY) && i + this.scroll == this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get())) {
                colorCode = TextFormatting.YELLOW.toString();
            } else {
                colorCode = TextFormatting.LIGHT_GRAY.toString();
            }

            int textX = (int) (leftMargin + 1 + (this.anchor.xPosition * (largestSuggestion - fontRenderer.getStringWidth(suggestionText))));

            fontRenderer.drawStringWithShadow(colorCode + suggestionText, textX, minY + suggestionBoxHeight - suggestionHeight + 3, 0xE0E0E0);
        }

        if (this.isHoveringOverSuggestions(mouseX, mouseY) && this.suggestions.get(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get()).getTooltip() != null) {
            this.tooltip.render(this.suggestions.get(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get()).getTooltip().getString(), mouseX, mouseY, 0, 0);
        }
    }

    private void renderSingleSuggestionLine(FontRenderer fontRenderer, String text, int heightIndex, boolean followParameters) {
        int leftMargin = this.xSupplier.get(this.textField, this, this.mc, Commander.suggestionsFollowParameters && followParameters);
        int stringWidth = fontRenderer.getStringWidth(text);

        leftMargin -= (int) (this.anchor.xPosition * (stringWidth + 2));

        int minY = this.ySupplier.get(this.textField, this, this.mc, Commander.suggestionsFollowParameters && followParameters);

        minY += (int) CommanderHelper.linearInterpolation(this.anchor.yPosition, heightIndex * 12, -12 - heightIndex * 12);

        this.drawRect(leftMargin, minY, stringWidth + leftMargin + 1, minY + 12, Integer.MIN_VALUE);
        fontRenderer.drawStringWithShadow(text, leftMargin + 1, minY + 2, 0xE0E0E0);
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

        int TAB = 15;
        int LEFT_SHIFT = 42;

        if (key == TAB) {
            if (this.commandIndex == -1 && this.isHoveringOverSuggestions(mouseX, mouseY)) {
                this.cycleToSuggestion(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get());
            } else if (Keyboard.isKeyDown(LEFT_SHIFT)) {
                this.cycleThroughSuggestions(-1);
            } else {
                this.cycleThroughSuggestions();
            }
            return;
        }

        if (!this.shouldApplySuggestion(c, key)) return;

        updateSuggestions();
    }

    public void updateSuggestions() {
        this.resetAllManagerVariables();
        String text = this.editor.getText();
        int cursor = this.editor.getCursor();
        if (this.parseResults != null && !this.parseResults.getReader().getString().equals(text)) {
            this.parseResults = null;
        }

        StringReader stringReader = new StringReader(text);

        if (this.mc.isMultiplayerWorld()) {
            if (ModVersionHelper.isModPresent("commander", EnumModList.SERVER)) {
                this.parseResults = null;
                this.mc.getSendQueue().addToSendQueue(new RequestCommandManagerPacket(this.mc.thePlayer.username, text, cursor));
            } else {
                Commander.serverSuggestions = CommanderHelper.getDefaultServerSuggestions();
            }
        } else {
            if (stringReader.canRead() && stringReader.peek() == '/') stringReader.skip();
            CommandDispatcher<CommanderCommandSource> dispatcher = this.getManager().getDispatcher();
            if (this.parseResults == null) {
                this.parseResults = dispatcher.parse(stringReader, this.commandSource);
            }
            if (cursor >= 1 || text.isEmpty()) {
                this.pendingSuggestions = dispatcher.getCompletionSuggestions(this.parseResults, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.finishUpdatingSuggestions();
                    }
                });
            }
        }
    }

    private boolean shouldApplySuggestion(char c, int key) {
        return this.textField.isCharacterAllowed(c)
                || ((key == 46 || key == 47) && (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)))
                || key == 199
                || key == 207
                || key == 203
                || key == 205
                || key == 14
                || key == 211;
    }

    private void finishUpdatingSuggestions() {
        this.suggestions = new ArrayList<>();
        if (this.hidden) return;
        if (!Commander.serverSuggestions.isEmpty()) {
            for (JsonElement jsonSuggestion : Commander.serverSuggestions.getAsJsonArray(CommandManagerPacketKeys.SUGGESTIONS)) {
                Suggestion suggestion;
                if (jsonSuggestion.getAsJsonObject().has(CommandManagerPacketKeys.TOOLTIP)) {
                    suggestion = new Suggestion(new StringRange(jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.RANGE).getAsJsonObject().get(CommandManagerPacketKeys.RANGE_START).getAsInt(), jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.RANGE).getAsJsonObject().get(CommandManagerPacketKeys.RANGE_END).getAsInt()), jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.VALUE).getAsString(), () -> jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.TOOLTIP).getAsString());
                } else {
                    suggestion = new Suggestion(new StringRange(jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.RANGE).getAsJsonObject().get(CommandManagerPacketKeys.RANGE_START).getAsInt(), jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.RANGE).getAsJsonObject().get(CommandManagerPacketKeys.RANGE_END).getAsInt()), jsonSuggestion.getAsJsonObject().get(CommandManagerPacketKeys.VALUE).getAsString());
                }
                if (this.tablessCursor <= this.tablessMessage.length() && suggestion.getText().startsWith(this.tablessMessage.substring(Math.min(suggestion.getRange().getStart(), this.tablessMessage.length()), Math.min(this.tablessCursor, Math.min(suggestion.getRange().getStart(), this.tablessMessage.length()))))) {
                    this.suggestions.add(suggestion);
                }
            }
        } else if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            Suggestions suggestions = this.pendingSuggestions.join();
            this.suggestions.addAll(suggestions.getList());
        }
    }

    public void updateScreen(int dWheel) {
        this.finishUpdatingSuggestions();
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
        if (this.suggestions.isEmpty() || this.hidden) return Optional.empty();

        int minX = this.xSupplier.get(this.textField, this, this.mc, Commander.suggestionsFollowParameters);

        int largestSuggestion = 0;
        for (Suggestion suggestion : this.suggestions)
            if (this.fontRenderer.getStringWidth(suggestion.getText()) > largestSuggestion) largestSuggestion = this.fontRenderer.getStringWidth(suggestion.getText());

        minX -= (int) (this.anchor.xPosition * (largestSuggestion + 2));

        int minY = this.ySupplier.get(this.textField, this, this.mc, Commander.suggestionsFollowParameters);
        int suggestionBoxHeight = Math.min(this.suggestions.size(), Commander.maxSuggestions) * 12;

        minY += (int) CommanderHelper.linearInterpolation(this.anchor.yPosition, 1, -suggestionBoxHeight - 1);

        int maxX = largestSuggestion + minX + 1;
        for (int i = 0; i < Math.min(this.suggestions.size(), Commander.maxSuggestions); i++) {
            int suggestionHeight = 12 * (-i + Math.min(this.suggestions.size(), Commander.maxSuggestions) - 2) + 25;
            int suggestionMinY = minY + suggestionBoxHeight - suggestionHeight + 2;
            int suggestionMaxY = suggestionMinY + 12;

            if (cursorX >= minX && cursorX < maxX && cursorY >= suggestionMinY && cursorY < suggestionMaxY) {
                return Optional.of(i + this.scroll);
            }
        }
        return Optional.empty();
    }

    private void resetAllManagerVariables() {
        this.commandIndex = -1;
        this.tablessMessage = this.textField.getText();
        this.tablessCursor = this.editor.getCursor();
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
        if (this.suggestions.isEmpty()) return;
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

    public int getSuggestionRangeStart() {
        if (!this.suggestions.isEmpty()) {
            return this.suggestions.get(0).getRange().getStart();
        } else if (Commander.serverSuggestions.has(CommandManagerPacketKeys.SUGGESTION_CONTEXT_START_POS)) {
            return Commander.serverSuggestions.get(CommandManagerPacketKeys.SUGGESTION_CONTEXT_START_POS).getAsInt();
        }
        return this.parseResults != null ? this.parseResults.getContext().findSuggestionContext(this.tablessCursor).startPos : 0;
    }

    public void setAnchor(ComponentAnchor anchor) {
        this.anchor = anchor;
    }

    public String colorCodeText(String text, boolean requireSlash) {
        if (this.parseResults != null && !this.parseResults.getReader().getString().equals(text)) {
            this.parseResults = null;
        }

        StringReader stringReader = new StringReader(text);
        boolean isCommand = (stringReader.canRead() && stringReader.peek() == '/') || !requireSlash;
        if (!isCommand || text.isEmpty()) return text;
        if (!Commander.serverSuggestions.isEmpty() && Commander.serverSuggestions.get(CommandManagerPacketKeys.LAST_CHILD) != null && Commander.serverSuggestions.get(CommandManagerPacketKeys.LAST_CHILD).getAsJsonObject().get(CommandManagerPacketKeys.ARGUMENTS) != null) {
            StringBuilder stringToDrawBuilder = new StringBuilder();
            int currentArgumentEnd = 0;
            int currentColor = 0;
            for (JsonElement jsonElement : Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.LAST_CHILD).getAsJsonArray(CommandManagerPacketKeys.ARGUMENTS)) {
                int rangeStart = jsonElement.getAsJsonObject().getAsJsonObject(CommandManagerPacketKeys.RANGE).get(CommandManagerPacketKeys.RANGE_START).getAsInt();
                int rangeEnd = jsonElement.getAsJsonObject().getAsJsonObject(CommandManagerPacketKeys.RANGE).get(CommandManagerPacketKeys.RANGE_END).getAsInt();

                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, Math.min(currentArgumentEnd, text.length()), Math.min(rangeStart, text.length()));
                stringToDrawBuilder.append(Commander.ARGUMENT_STYLES.get(currentColor)).append(text, Math.min(rangeStart, text.length()), Math.min(rangeEnd, text.length()));

                currentArgumentEnd = rangeEnd;
                if (++currentColor >= Commander.ARGUMENT_STYLES.size()) {
                    currentColor = 0;
                }
            }
            if (Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.READER).get(CommandManagerPacketKeys.READER_CAN_READ).getAsBoolean() && currentArgumentEnd < text.length()) {
                int remainingTextLength = Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.READER).get(CommandManagerPacketKeys.READER_REMAINING_TEXT_LENGTH).getAsInt();
                stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text, currentArgumentEnd, Math.min(Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.READER).get(CommandManagerPacketKeys.READER_CURSOR).getAsInt(), text.length()));
                stringToDrawBuilder.append(TextFormatting.RED).append(text, Math.min(Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.READER).get(CommandManagerPacketKeys.READER_CURSOR).getAsInt(), text.length()), Math.min(remainingTextLength, text.length()));
                currentArgumentEnd = remainingTextLength;
            }
            stringToDrawBuilder.append(TextFormatting.LIGHT_GRAY).append(text.substring(Math.min(currentArgumentEnd, text.length())));

            return stringToDrawBuilder.toString();
        } else {
            if (stringReader.canRead() && stringReader.peek() == '/') stringReader.skip();
            CommandDispatcher<CommanderCommandSource> dispatcher = this.getManager().getDispatcher();
            if (this.parseResults == null) {
                this.parseResults = dispatcher.parse(stringReader, this.getCommandSource());
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
            return stringToDrawBuilder.toString();
        }
    }

    public String getSuggestionPreview() {
        int mouseX = GuiHelper.getScaledMouseX(this.mc);
        int mouseY = GuiHelper.getScaledMouseY(this.mc) - 1;

        if (this.shouldDrawSuggestionPreview()) {
            Suggestion suggestionToRender = null;
            if (this.isHoveringOverSuggestions(mouseX, mouseY)) {
                suggestionToRender = this.getSuggestions().get(this.getIndexOfSuggestionBeingHoveredOver(mouseX, mouseY).get());
            } else if (!this.getSuggestions().isEmpty()) {
                suggestionToRender = this.getSuggestions().get(0);
            }

            if (suggestionToRender != null && suggestionToRender.getText().startsWith(this.editor.getText().substring(Math.min(suggestionToRender.getRange().getStart(), this.editor.getText().length())))) {
                return TextFormatting.LIGHT_GRAY + suggestionToRender.getText();
            }
        }
        return "";
    }

    @Unique
    private boolean shouldDrawSuggestionPreview() {
        boolean basedOnParseResults = this.parseResults != null && (this.tablessCursor == this.parseResults.getReader().getString().trim().length() || this.tablessCursor == this.parseResults.getReader().getString().length());
        boolean basedOnServer = Commander.serverSuggestions.has(CommandManagerPacketKeys.READER) && (this.tablessCursor == Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.READER).get(CommandManagerPacketKeys.READER_STRING).getAsString().trim().length() || this.tablessCursor == Commander.serverSuggestions.getAsJsonObject(CommandManagerPacketKeys.READER).get(CommandManagerPacketKeys.READER_STRING).getAsString().length());
        return basedOnParseResults || basedOnServer;
    }

    public int getDefaultParameterPosition() {
        return this.fontRenderer.getStringWidth(this.getMessage().substring(0, Math.min(this.getSuggestionRangeStart(), this.getMessage().length()))) + 2;
    }
}
