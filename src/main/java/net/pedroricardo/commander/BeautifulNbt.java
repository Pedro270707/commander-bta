package net.pedroricardo.commander;

import com.mojang.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BeautifulNbt {
    public static String toBeautifulNbt(@NotNull CompoundTag nbt) {
        StringBuilder builder = new StringBuilder("{");
        boolean comma = false;
        for (Tag<?> tag : nbt.getValue().values()) {
            if (comma) builder.append(", ");
            builder.append(tag.getTagName()).append(": ").append(element(tag instanceof CompoundTag ? tag : tag.getValue()));
            comma = true;
        }
        return builder.append("}").toString();
    }

    private static String element(Object o) {
        if (o instanceof byte[] || o instanceof short[] || o instanceof double[] || o instanceof List<?>) {
            return collection(o);
        }
        if (o instanceof Byte) {
            return o + "b";
        }
        if (o instanceof Short) {
            return o + "s";
        }
        if (o instanceof Long) {
            return o + "l";
        }
        if (o instanceof Float) {
            return o + "f";
        }
        if (o instanceof String) {
            return "\"" + o + "\"";
        }
        return o instanceof CompoundTag ? toBeautifulNbt((CompoundTag) o) : o.toString();
    }

    private static String collection(Object o) {
        StringBuilder result = new StringBuilder("[");
        boolean comma = false;
        if (o instanceof byte[]) {
            result.append("B; ");
            for (double element : ((byte[])o)) {
                if (comma) result.append(", ");
                result.append(element(element));
                comma = true;
            }
        } else if (o instanceof short[]) {
            result.append("S; ");
            for (double element : ((short[])o)) {
                if (comma) result.append(", ");
                result.append(element(element));
                comma = true;
            }
        } else if (o instanceof double[]) {
            result.append("D; ");
            for (double element : ((double[])o)) {
                if (comma) result.append(", ");
                result.append(element(element));
                comma = true;
            }
        } else if (o instanceof List<?>) {
            //noinspection unchecked
            for (Tag<?> element : ((List<Tag<?>>)o)) {
                if (comma) result.append(", ");
                result.append(element(element.getValue()));
                comma = true;
            }
        }
        return result.append("]").toString();
    }
}
