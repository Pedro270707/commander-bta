package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemLabel;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.collection.Pair;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.CommanderHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ItemStackArgumentParser {
    private static final SimpleCommandExceptionType INVALID_ITEM = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_item"));
    private static final DynamicCommandExceptionType EXPECTED_TAG_KEY_VALUE = new DynamicCommandExceptionType((value) -> () -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.valueless", value));
    private static final SimpleCommandExceptionType INVALID_TAG_KEY_VALUE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.invalid"));
    private static final SimpleCommandExceptionType EXPECTED_END_OF_METADATA = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.metadata.unterminated"));
    private static final SimpleCommandExceptionType EXPECTED_END_OF_TAG = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.unterminated"));

    private final StringReader reader;
    private int startPosition = 0;

    private Item item;
    private int stackSize = 1;
    private int metadata = 0;
    private CompoundTag tag = new CompoundTag();

    private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = CommanderHelper.NO_SUGGESTIONS;

    public ItemStackArgumentParser(StringReader reader) {
        this.reader = reader;
    }

    private CompletableFuture<Suggestions> suggestItems(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(this.startPosition);
        consumer.accept(suggestionsBuilder2);
        return suggestionsBuilder.add(suggestionsBuilder2).buildFuture();
    }
    
    public ItemStack parse() throws CommandSyntaxException {
        this.startPosition = this.reader.getCursor();

        this.suggestions = this::suggestItems;
        this.parseItem();

        if (this.item == null) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(this.reader);

        this.suggestions = this::suggestOpenMetadataOrTag;
        if (this.reader.canRead() && this.reader.peek() == '[') {
            this.parseMetadata();
        }

        if (this.reader.canRead() && this.reader.peek() == '{') {
            this.parseTag();
        }

        if (this.item != null) {
            return new ItemStack(this.item, this.stackSize, this.metadata, this.tag);
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_block"));
    }

    private void parseItem() throws CommandSyntaxException {
        String string = this.reader.readString();
        for (Item itemInList : Item.itemsList) {
            if (itemInList == null) continue;
            if (CommanderHelper.matchesKeyString(itemInList.getKey(), string)) {
                this.item = itemInList;
            }
        }
    }

    private void parseMetadata() throws CommandSyntaxException {
        this.suggestions = CommanderHelper.NO_SUGGESTIONS;
        this.reader.skip();
        this.reader.skipWhitespace();
        int cursor = this.reader.getCursor();
        int metadata = this.reader.readInt();
        if (metadata < 0) {
            this.reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(this.reader, metadata, 0);
        } else if (metadata > 255) {
            this.reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(this.reader, metadata, 255);
        }
        this.reader.skipWhitespace();
        this.suggestions = this::suggestCloseMetadata;
        if (this.reader.canRead() && this.reader.peek() == ']') {
            this.suggestions = this::suggestOpenTag;
            this.reader.skip();
            this.metadata = metadata;
        } else {
            throw EXPECTED_END_OF_METADATA.createWithContext(this.reader);
        }
    }

    private void parseTag() throws CommandSyntaxException {
        int cursor = this.reader.getCursor();
        this.suggestions = this::suggestCloseTag;
        this.reader.skip();
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != '}') {
            String key = this.reader.readString();
            this.reader.skipWhitespace();
            this.suggestions = this::suggestTagSeparator;
            if (!this.reader.canRead() || this.reader.peek() != ':') {
                this.reader.setCursor(cursor);
                throw EXPECTED_TAG_KEY_VALUE.createWithContext(this.reader, key);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = this::suggestCloseTag;
            try {
                int value = this.reader.readInt();
                this.tag.putInt(key, value);
            } catch (CommandSyntaxException e1) {
                try {
                    float value = this.reader.readFloat();
                    this.tag.putFloat(key, value);
                } catch (CommandSyntaxException e2) {
                    try {
                        double value = this.reader.readDouble();
                        this.tag.putDouble(key, value);
                    } catch (CommandSyntaxException e3) {
                        try {
                            long value = this.reader.readLong();
                            this.tag.putLong(key, value);
                        } catch (CommandSyntaxException e4) {
                            try {
                                boolean value = this.reader.readBoolean();
                                this.tag.putBoolean(key, value);
                            } catch (CommandSyntaxException e5) {
                                try {
                                    String value = this.reader.readString();
                                    this.tag.putString(key, value);
                                } catch (CommandSyntaxException e6) {
                                    this.reader.setCursor(cursor);
                                    throw INVALID_TAG_KEY_VALUE.createWithContext(this.reader);
                                }
                            }
                        }
                    }
                }
            }
            this.reader.skipWhitespace();
            if (this.reader.canRead() && this.reader.peek() == ',') {
                this.reader.skip();
                this.reader.skipWhitespace();
                this.suggestions = CommanderHelper.NO_SUGGESTIONS;
                continue;
            }
            if (this.reader.canRead() && this.reader.peek() == '}') break;
            throw EXPECTED_END_OF_TAG.createWithContext(this.reader);
        }
        if (!this.reader.canRead()) throw EXPECTED_END_OF_TAG.createWithContext(this.reader);
        this.suggestions = CommanderHelper.NO_SUGGESTIONS;
        this.reader.skip();
    }

    private CompletableFuture<Suggestions> suggestOpenMetadataOrTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) {
            suggestionsBuilder.suggest(String.valueOf('['));
            suggestionsBuilder.suggest(String.valueOf('{'));
        }
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf('{'));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCloseMetadata(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf(']'));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCloseTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf('}'));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTagSeparator(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf(':'));
        return suggestionsBuilder.buildFuture();
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }
}
