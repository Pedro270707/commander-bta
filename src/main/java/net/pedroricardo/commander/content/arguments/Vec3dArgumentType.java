package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.DoubleCoordinate;
import net.pedroricardo.commander.content.helpers.DoublePos;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class Vec3dArgumentType implements ArgumentType<Vec3d> {
    private static final List<String> EXAMPLES = Arrays.asList("0 0 0", "15.0 6.0 12.5", "3.141 59.26 58.97");

    public static ArgumentType<Vec3d> vec3d() {
        return new Vec3dArgumentType();
    }

    @Override
    public Vec3d parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        if (!reader.canRead()) throw CommanderExceptions.incomplete().createWithContext(reader);
        double x;
        double y;
        double z;
        if (reader.peek() != ' ') {
            x = reader.readDouble();
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                if (reader.canRead() && reader.peek() != ' ') {
                    y = reader.readDouble();
                    if (reader.canRead() && reader.peek() == ' ') {
                        reader.skip();
                        if (reader.canRead() && reader.peek() != ' ') {
                            z = reader.readDouble();
                            return Vec3d.createVector(x, y, z);
                        }
                    }
                }
            }
        }
        reader.setCursor(i);
        throw CommanderExceptions.incomplete().createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String string = builder.getRemaining();
        Vec3d coordinates = Vec3d.createVector(0.0, 0.0, 0.0);

        if (string.isEmpty()) {
            coordinates.xCoord = roundToSixDecimals(coordinates.xCoord);
            coordinates.yCoord = roundToSixDecimals(coordinates.yCoord);
            coordinates.zCoord = roundToSixDecimals(coordinates.zCoord);
            String allCoordinates = coordinates.xCoord + " " + coordinates.yCoord + " " + coordinates.zCoord;
            try {
                this.parse(new StringReader(allCoordinates));
                builder.suggest(String.valueOf(coordinates.xCoord));
                builder.suggest(coordinates.xCoord + " " + coordinates.yCoord);
                builder.suggest(allCoordinates);
            } catch (CommandSyntaxException ignored) {}
        } else {
            String[] strings = string.split(" ");
            String allCoordinates;
            switch (strings.length) {
                case 1:
                    allCoordinates = strings[0] + " " + coordinates.yCoord + " " + coordinates.zCoord;
                    try {
                        this.parse(new StringReader(allCoordinates));
                        builder.suggest(strings[0] + " " + coordinates.yCoord);
                        builder.suggest(allCoordinates);
                    } catch (CommandSyntaxException ignored) {}
                    break;
                case 2:
                    allCoordinates = strings[0] + " " + strings[1] + " " + coordinates.zCoord;
                    try {
                        this.parse(new StringReader(allCoordinates));
                        builder.suggest(allCoordinates);
                    } catch (CommandSyntaxException ignored) {}
                    break;
            }
        }
        return builder.buildFuture();
    }

    private static double roundToSixDecimals(double value) {
        DecimalFormat df = new DecimalFormat("#.######", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return Double.parseDouble(df.format(value));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
