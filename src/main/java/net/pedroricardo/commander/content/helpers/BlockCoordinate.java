package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import org.jetbrains.annotations.Nullable;

public class BlockCoordinate {
    private final boolean isRelative;
    private final int coordinate;

    public BlockCoordinate(boolean isRelative, int coordinate) {
        this.isRelative = isRelative;
        this.coordinate = coordinate;
    }

    public int get(@Nullable Integer sourceCoordinate) throws CommandSyntaxException {
        if (this.isRelative) {
            if (sourceCoordinate != null) {
                return sourceCoordinate + this.coordinate;
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }

        return this.coordinate;
    }

    public static BlockCoordinate parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) throw CommanderExceptions.incomplete().createWithContext(reader);

        if (reader.peek() == '~') {
            reader.skip();
            if (reader.canRead() && reader.peek() != ' ') {
                int coordinate = reader.readInt();
                return new BlockCoordinate(true, coordinate);
            } else {
                return new BlockCoordinate(true, 0);
            }
        } else if (reader.peek() != ' ') {
            int coordinate = reader.readInt();
            return new BlockCoordinate(false, coordinate);
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedInt().createWithContext(reader);
    }

    public boolean isRelative() {
        return this.isRelative;
    }
}
