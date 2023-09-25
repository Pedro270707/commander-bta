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
import net.pedroricardo.commander.content.helpers.IntegerCoordinate;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IntegerCoordinatesArgumentType implements ArgumentType<IntegerCoordinates> {
    private static final List<String> EXAMPLES = Arrays.asList("~ ~ ~", "0 0 0", "~ ~60 ~", "~-20 ~10 -25");

    public static IntegerCoordinatesArgumentType intCoordinates() {
        return new IntegerCoordinatesArgumentType();
    }

    @Override
    public IntegerCoordinates parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        IntegerCoordinate x = IntegerCoordinate.parse(reader);
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
        IntegerCoordinate y = IntegerCoordinate.parse(reader);
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
        IntegerCoordinate z = IntegerCoordinate.parse(reader);
        return new IntegerCoordinates(x, y, z);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String string = builder.getRemaining();
        Vec3d coordinates = ((CommanderCommandSource)context.getSource()).getBlockCoordinates();

        if (coordinates == null) return builder.buildFuture();

        // Rounding the coordinates
        int[] roundedCoordinates = new int[]{(int)Math.floor(coordinates.xCoord), (int)Math.floor(coordinates.yCoord), (int)Math.floor(coordinates.zCoord)};

        if (string.isEmpty()) {
            String allCoordinates = roundedCoordinates[0] + " " + roundedCoordinates[1] + " " + roundedCoordinates[2];
            try {
                this.parse(new StringReader(allCoordinates));
                builder.suggest(String.valueOf(roundedCoordinates[0]));
                builder.suggest(roundedCoordinates[0] + " " + roundedCoordinates[1]);
                builder.suggest(allCoordinates);
            } catch (CommandSyntaxException ignored) {}
        } else {
            String[] strings = string.split(" ");
            String allCoordinates;
            switch (strings.length) {
                case 1:
                    allCoordinates = strings[0] + " " + roundedCoordinates[1] + " " + roundedCoordinates[2];
                    try {
                        this.parse(new StringReader(allCoordinates));
                        builder.suggest(strings[0] + " " + roundedCoordinates[1]);
                        builder.suggest(allCoordinates);
                    } catch (CommandSyntaxException ignored) {}
                    break;
                case 2:
                    allCoordinates = strings[0] + " " + strings[1] + " " + roundedCoordinates[2];
                    try {
                        this.parse(new StringReader(allCoordinates));
                        builder.suggest(allCoordinates);
                    } catch (CommandSyntaxException ignored) {}
                    break;
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
