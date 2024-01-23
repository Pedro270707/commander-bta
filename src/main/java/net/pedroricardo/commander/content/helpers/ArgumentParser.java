package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.nbt.*;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class ArgumentParser {
    private static final DynamicCommandExceptionType EXPECTED_TAG_KEY_VALUE = new DynamicCommandExceptionType((value) -> () -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.valueless", value));
    private static final SimpleCommandExceptionType INVALID_TAG_KEY_VALUE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.invalid"));
    private static final SimpleCommandExceptionType EXPECTED_END_OF_METADATA = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.metadata.unterminated"));
    private static final SimpleCommandExceptionType EXPECTED_END_OF_TAG = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKeyAndFormat("argument_types.commander.item_stack.tag.unterminated"));

    private static final Pattern DOUBLE_PATTERN_NO_SUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");

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

    protected CompoundTag parseCompound() throws CommandSyntaxException {
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
            this.suggestions = CommanderHelper.NO_SUGGESTIONS;
            Tag<?> value = this.parseTag();
            tag.put(key, value);
            this.reader.skipWhitespace();
            this.suggestions = this::suggestCloseTag;
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

    private Tag<?> parseTag() throws CommandSyntaxException {
        if (!this.reader.canRead()) throw CommanderExceptions.incomplete().create();
        if (this.reader.peek() == '{') {
            return this.parseCompound();
        } else if (this.reader.peek() == '[') {
            this.reader.skip();
            return collection(this.reader);
        }
        return type(this.reader.readString());
    }

    private Tag<?> collection(StringReader stringReader) throws CommandSyntaxException {
        stringReader.skipWhitespace();
        if (!stringReader.canRead()) throw CommanderExceptions.incomplete().create();
        if (stringReader.peek() == 'B') {
            stringReader.skip();
            stringReader.skipWhitespace();
            if (!stringReader.canRead()) throw CommanderExceptions.incomplete().create();
            if (stringReader.peek() == ']') return new ByteArrayTag();
            else if (stringReader.peek() == ';') {
                stringReader.skip();
                List<Byte> bytes = new ArrayList<>();
                while (stringReader.canRead() && stringReader.peek() != ']') {
                    stringReader.skipWhitespace();
                    String byteAsString = stringReader.readString();
                    if (!BYTE_PATTERN.matcher(byteAsString).matches()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().create("byte");
                    byte parsedByte = Byte.parseByte(byteAsString.substring(0, byteAsString.length() - 1));
                    bytes.add(parsedByte);
                    stringReader.skipWhitespace();
                    if (!stringReader.canRead() || stringReader.peek() != ',') break;
                    stringReader.skip();
                }
                stringReader.skipWhitespace();
                if (stringReader.canRead() && stringReader.peek() == ']') {
                    stringReader.skip();
                    return new ByteArrayTag(byteListToArray(bytes));
                }
                throw EXPECTED_END_OF_TAG.create();
            }
        } else if (stringReader.peek() == 'S') {
            stringReader.skip();
            stringReader.skipWhitespace();
            if (!stringReader.canRead()) throw CommanderExceptions.incomplete().create();
            if (stringReader.peek() == ']') {
                stringReader.skip();
                return new ShortArrayTag();
            }
            else if (stringReader.peek() == ';') {
                stringReader.skip();
                List<Short> shorts = new ArrayList<>();
                while (stringReader.canRead() && stringReader.peek() != ']') {
                    stringReader.skipWhitespace();
                    String shortAsString = stringReader.readString();
                    if (!SHORT_PATTERN.matcher(shortAsString).matches()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().create("short");
                    short parsedShort = Short.parseShort(shortAsString.substring(0, shortAsString.length() - 1));
                    shorts.add(parsedShort);
                    stringReader.skipWhitespace();
                    if (!stringReader.canRead() || stringReader.peek() != ',') break;
                    stringReader.skip();
                }
                stringReader.skipWhitespace();

                if (stringReader.canRead() && stringReader.peek() == ']') {
                    stringReader.skip();
                    return new ShortArrayTag(ArgumentParser.shortListToArray(shorts));
                }
                else throw EXPECTED_END_OF_TAG.create();
            }
        } else if (stringReader.peek() == 'D') {
            stringReader.skip();
            stringReader.skipWhitespace();
            if (!stringReader.canRead()) throw CommanderExceptions.incomplete().create();
            if (stringReader.peek() == ']') {
                stringReader.skip();
                return new DoubleArrayTag();
            }
            else if (stringReader.peek() == ';') {
                stringReader.skip();
                List<Double> doubles = new ArrayList<>();
                while (stringReader.canRead() && stringReader.peek() != ']') {
                    stringReader.skipWhitespace();
                    String doubleAsString = stringReader.readString();
                    if (!SHORT_PATTERN.matcher(doubleAsString).matches()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().create("short");
                    double parsedDouble = Double.parseDouble(doubleAsString.substring(0, doubleAsString.length() - 1));
                    doubles.add(parsedDouble);
                    stringReader.skipWhitespace();
                    if (!stringReader.canRead() || stringReader.peek() != ',') break;
                    stringReader.skip();
                }
                stringReader.skipWhitespace();
                if (stringReader.canRead() && stringReader.peek() == ']') {
                    stringReader.skip();
                    return new DoubleArrayTag(doubles.stream().mapToDouble(Double::doubleValue).toArray());
                }
                else throw EXPECTED_END_OF_TAG.create();
            }
        } else {
            List<Tag<?>> tags = new ArrayList<>();
            while (stringReader.canRead() && stringReader.peek() != ']') {
                stringReader.skipWhitespace();
                tags.add(this.parseTag());
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') break;
                stringReader.skip();
            }
            stringReader.skipWhitespace();
            if (stringReader.canRead() && stringReader.peek() == ']') {
                stringReader.skip();
                return new ListTag(tags);
            }
            else throw EXPECTED_END_OF_TAG.create();
        }
        throw CommanderExceptions.incomplete().create();
    }

    private static short[] shortListToArray(List<Short> shorts) {
        short[] arr = new short[shorts.size()];

        for (int i = 0; i < shorts.size(); i++) {
            arr[i] = shorts.get(i);
        }

        return arr;
    }

    private static byte[] byteListToArray(List<Byte> bytes) {
        byte[] arr = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            arr[i] = bytes.get(i);
        }

        return arr;
    }

    private static Tag<?> type(String string) {
        try {
            if ("true".equalsIgnoreCase(string)) {
                return new ByteTag((byte) 1);
            }
            if ("false".equalsIgnoreCase(string)) {
                return new ByteTag((byte) 0);
            }
            if (BYTE_PATTERN.matcher(string).matches()) {
                return new ByteTag(Byte.parseByte(string.substring(0, string.length() - 1)));
            }
            if (SHORT_PATTERN.matcher(string).matches()) {
                return new ShortTag(Short.parseShort(string.substring(0, string.length() - 1)));
            }
            if (INT_PATTERN.matcher(string).matches()) {
                return new IntTag(Integer.parseInt(string));
            }
            if (LONG_PATTERN.matcher(string).matches()) {
                return new LongTag(Long.parseLong(string.substring(0, string.length() - 1)));
            }
            if (FLOAT_PATTERN.matcher(string).matches()) {
                return new FloatTag(Float.parseFloat(string.substring(0, string.length() - 1)));
            }
            if (DOUBLE_PATTERN.matcher(string).matches()) {
                return new DoubleTag(Double.parseDouble(string.substring(0, string.length() - 1)));
            }
            if (DOUBLE_PATTERN_NO_SUFFIX.matcher(string).matches()) {
                return new DoubleTag(Double.parseDouble(string));
            }
        } catch (NumberFormatException ignored) {
        }
        return new StringTag(string);
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
