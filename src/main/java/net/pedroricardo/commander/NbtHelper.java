package net.pedroricardo.commander;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.nbt.*;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NbtHelper {
    private static final Pattern DOUBLE_PATTERN_NO_SUFFIX = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", 2);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", 2);
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", 2);
    private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", 2);
    private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", 2);
    private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", 2);
    private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");

    public static CompoundTag parseNbt(String string) throws CommandSyntaxException {
        return parseNbt(new StringReader(string));
    }

    public static CompoundTag parseNbt(StringReader reader) throws CommandSyntaxException {
        CompoundTag tag = new CompoundTag();
        if (!reader.canRead() || reader.peek() != '{') return tag;
        int cursor = reader.getCursor();
        reader.skip();
        reader.skipWhitespace();
        while (reader.canRead() && reader.peek() != '}') {
            String key = reader.readString();
            reader.skipWhitespace();
            if (!reader.canRead() || reader.peek() != ':') {
                reader.setCursor(cursor);
                throw CommanderExceptions.expectedTagKeyValue().createWithContext(reader, key);
            }
            reader.skip();
            reader.skipWhitespace();
            Tag<?> value = parseTag(reader);
            tag.put(key, value);
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() == ',') {
                reader.skip();
                reader.skipWhitespace();
                continue;
            }
            if (reader.canRead() && reader.peek() == '}') break;
            throw CommanderExceptions.expectedEndOfTag().createWithContext(reader);
        }
        if (!reader.canRead()) throw CommanderExceptions.expectedEndOfTag().createWithContext(reader);
        reader.skip();
        return tag;
    }

    private static Tag<?> parseTag(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) throw CommanderExceptions.incomplete().create();
        if (reader.peek() == '{') {
            return parseNbt(reader);
        } else if (reader.peek() == '[') {
            reader.skip();
            return collection(reader);
        }
        return type(reader.readString());
    }

    private static Tag<?> collection(StringReader reader) throws CommandSyntaxException {
        reader.skipWhitespace();
        if (!reader.canRead()) throw CommanderExceptions.incomplete().create();
        if (reader.peek() == 'B') {
            reader.skip();
            reader.skipWhitespace();
            if (!reader.canRead()) throw CommanderExceptions.incomplete().create();
            if (reader.peek() == ']') return new ByteArrayTag();
            else if (reader.peek() == ';') {
                reader.skip();
                List<Byte> bytes = new ArrayList<>();
                while (reader.canRead() && reader.peek() != ']') {
                    reader.skipWhitespace();
                    String byteAsString = reader.readString();
                    if (!BYTE_PATTERN.matcher(byteAsString).matches()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().create("byte");
                    byte parsedByte = Byte.parseByte(byteAsString.substring(0, byteAsString.length() - 1));
                    bytes.add(parsedByte);
                    reader.skipWhitespace();
                    if (!reader.canRead() || reader.peek() != ',') break;
                    reader.skip();
                }
                reader.skipWhitespace();
                if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    return new ByteArrayTag(byteListToArray(bytes));
                }
                throw CommanderExceptions.expectedEndOfTag().create();
            }
        } else if (reader.peek() == 'S') {
            reader.skip();
            reader.skipWhitespace();
            if (!reader.canRead()) throw CommanderExceptions.incomplete().create();
            if (reader.peek() == ']') {
                reader.skip();
                return new ShortArrayTag();
            }
            else if (reader.peek() == ';') {
                reader.skip();
                List<Short> shorts = new ArrayList<>();
                while (reader.canRead() && reader.peek() != ']') {
                    reader.skipWhitespace();
                    String shortAsString = reader.readString();
                    if (!SHORT_PATTERN.matcher(shortAsString).matches()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().create("short");
                    short parsedShort = Short.parseShort(shortAsString.substring(0, shortAsString.length() - 1));
                    shorts.add(parsedShort);
                    reader.skipWhitespace();
                    if (!reader.canRead() || reader.peek() != ',') break;
                    reader.skip();
                }
                reader.skipWhitespace();

                if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    return new ShortArrayTag(shortListToArray(shorts));
                }
                else throw CommanderExceptions.expectedEndOfTag().create();
            }
        } else if (reader.peek() == 'D') {
            reader.skip();
            reader.skipWhitespace();
            if (!reader.canRead()) throw CommanderExceptions.incomplete().create();
            if (reader.peek() == ']') {
                reader.skip();
                return new DoubleArrayTag();
            }
            else if (reader.peek() == ';') {
                reader.skip();
                List<Double> doubles = new ArrayList<>();
                while (reader.canRead() && reader.peek() != ']') {
                    reader.skipWhitespace();
                    String doubleAsString = reader.readString();
                    double parsedDouble;
                    if (DOUBLE_PATTERN.matcher(doubleAsString).matches()) {
                        parsedDouble = Double.parseDouble(doubleAsString.substring(0, doubleAsString.length() - 1));
                    } else if (DOUBLE_PATTERN_NO_SUFFIX.matcher(doubleAsString).matches()) {
                        parsedDouble = Double.parseDouble(doubleAsString);
                    }
                    else throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedDouble().create();
                    doubles.add(parsedDouble);
                    reader.skipWhitespace();
                    if (!reader.canRead() || reader.peek() != ',') break;
                    reader.skip();
                }
                reader.skipWhitespace();
                if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    return new DoubleArrayTag(doubles.stream().mapToDouble(Double::doubleValue).toArray());
                }
                else throw CommanderExceptions.expectedEndOfTag().create();
            }
        } else {
            List<Tag<?>> tags = new ArrayList<>();
            while (reader.canRead() && reader.peek() != ']') {
                reader.skipWhitespace();
                tags.add(parseTag(reader));
                reader.skipWhitespace();
                if (!reader.canRead() || reader.peek() != ',') break;
                reader.skip();
            }
            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() == ']') {
                reader.skip();
                return new ListTag(tags);
            }
            else throw CommanderExceptions.expectedEndOfTag().create();
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
}