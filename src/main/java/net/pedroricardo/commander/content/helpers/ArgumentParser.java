package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class ArgumentParser {
    private static final DynamicCommandExceptionType EXPECTED_TAG_KEY_VALUE = new DynamicCommandExceptionType((value) -> () -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.valueless", value));
    private static final SimpleCommandExceptionType INVALID_TAG_KEY_VALUE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.invalid"));
    private static final SimpleCommandExceptionType EXPECTED_END_OF_METADATA = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.metadata.unterminated"));
    private static final SimpleCommandExceptionType EXPECTED_END_OF_TAG = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.unterminated"));

    protected StringReader reader;
    protected int startPosition = 0;
    protected BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = CommanderHelper.NO_SUGGESTIONS;

    protected ArgumentParser(StringReader reader) {
        this.reader = reader;
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }

    protected int parseMetadata() throws CommandSyntaxException {
        this.suggestions = CommanderHelper.NO_SUGGESTIONS;
        this.reader.skip();
        this.reader.skipWhitespace();
        int cursor = this.reader.getCursor();
        int metadata = this.reader.readInt();
        if (metadata < 0) {
            this.reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(this.reader, metadata, 0);
        }
        this.reader.skipWhitespace();
        this.suggestions = this::suggestCloseMetadata;
        if (this.reader.canRead() && this.reader.peek() == ']') {
            this.suggestions = this::suggestOpenTag;
            this.reader.skip();
            return metadata;
        } else {
            throw EXPECTED_END_OF_METADATA.createWithContext(this.reader);
        }
    }

    protected CompoundTag parseTag() throws CommandSyntaxException {
        CompoundTag tag = new CompoundTag();
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
                tag.putInt(key, value);
            } catch (CommandSyntaxException e1) {
                try {
                    float value = this.reader.readFloat();
                    tag.putFloat(key, value);
                } catch (CommandSyntaxException e2) {
                    try {
                        double value = this.reader.readDouble();
                        tag.putDouble(key, value);
                    } catch (CommandSyntaxException e3) {
                        try {
                            long value = this.reader.readLong();
                            tag.putLong(key, value);
                        } catch (CommandSyntaxException e4) {
                            try {
                                boolean value = this.reader.readBoolean();
                                tag.putBoolean(key, value);
                            } catch (CommandSyntaxException e5) {
                                try {
                                    String value = this.reader.readString();
                                    tag.putString(key, value);
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
        return tag;
    }

    protected CompletableFuture<Suggestions> suggestOpenMetadataOrTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) {
            suggestionsBuilder.suggest(String.valueOf('['));
            suggestionsBuilder.suggest(String.valueOf('{'));
        }
        return suggestionsBuilder.buildFuture();
    }

    protected CompletableFuture<Suggestions> suggestOpenTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf('{'));
        return suggestionsBuilder.buildFuture();
    }

    protected CompletableFuture<Suggestions> suggestOpenMetadata(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf('['));
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
}
