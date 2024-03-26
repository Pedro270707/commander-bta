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

public class PositionArgumentType implements ArgumentType<DoublePos> {
    private static final List<String> EXAMPLES = Arrays.asList("~ ~ ~", "0 0 0", "~ ~60 ~", "~-20 ~10 ~-25.5");

    public static PositionArgumentType pos() {
        return new PositionArgumentType();
    }

    @Override
    public DoublePos parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        DoubleCoordinate x = DoubleCoordinate.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            if (reader.peek() == 'f' || reader.peek() == 'd') {
                reader.skip();
                if (!reader.canRead() || reader.peek() != ' ') {
                    reader.setCursor(i);
                    throw CommanderExceptions.incomplete().createWithContext(reader);
                }
            } else {
                reader.setCursor(i);
                throw CommanderExceptions.incomplete().createWithContext(reader);
            }
        }
        reader.skip();
        DoubleCoordinate y = DoubleCoordinate.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            if (reader.peek() == 'f' || reader.peek() == 'd') {
                reader.skip();
                if (!reader.canRead() || reader.peek() != ' ') {
                    reader.setCursor(i);
                    throw CommanderExceptions.incomplete().createWithContext(reader);
                }
            } else {
                reader.setCursor(i);
                throw CommanderExceptions.incomplete().createWithContext(reader);
            }
        }
        reader.skip();
        DoubleCoordinate z = DoubleCoordinate.parse(reader);
        return new DoublePos(x, y, z);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String string = builder.getRemaining();
        Vec3d coordinates = ((CommanderCommandSource)context.getSource()).getCoordinates(true);

        if (coordinates == null) return builder.buildFuture();

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