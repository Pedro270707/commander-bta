package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

public class DoubleCoordinate {
    private final boolean isRelative;
    private final double coordinate;

    public DoubleCoordinate(boolean isRelative, double coordinate) {
        this.isRelative = isRelative;
        this.coordinate = coordinate;
    }

    public double get(Double sourceCoordinate) throws CommandSyntaxException {
        if (this.isRelative) {
            if (sourceCoordinate != null) {
                return sourceCoordinate + this.coordinate;
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }

        return this.coordinate;
    }

    public static DoubleCoordinate parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) throw CommanderExceptions.incomplete().createWithContext(reader);

        if (reader.peek() == '~') {
            reader.skip();
            if (reader.canRead() && reader.peek() != ' ') {
                double coordinate = reader.readDouble();
                return new DoubleCoordinate(true, coordinate);
            } else {
                return new DoubleCoordinate(true, 0.0);
            }
        } else if (reader.peek() != ' ') {
            double coordinate = reader.readDouble();
            return new DoubleCoordinate(false, coordinate);
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedDouble().createWithContext(reader);
    }

    public boolean isRelative() {
        return this.isRelative;
    }
}
