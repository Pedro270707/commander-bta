package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.Coordinates2D;
import net.pedroricardo.commander.content.helpers.IntegerCoordinate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkCoordinatesArgumentType implements ArgumentType<Coordinates2D> {
    private static final List<String> EXAMPLES = Arrays.asList("~ ~", "0 0 0", "~60 ~", "~-20 -25");

    public static ChunkCoordinatesArgumentType chunkCoordinates() {
        return new ChunkCoordinatesArgumentType();
    }

    @Override
    public Coordinates2D parse(StringReader reader) throws CommandSyntaxException {
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
        IntegerCoordinate z = IntegerCoordinate.parse(reader);
        return new Coordinates2D(x, z);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String string = builder.getRemaining();
        Vec3d coordinates = ((CommanderCommandSource)context.getSource()).getBlockCoordinates();

        if (coordinates == null) return builder.buildFuture();

        int[] roundedCoordinates = new int[]{
                MathHelper.floor_double(coordinates.xCoord / 16.0),
                MathHelper.floor_double(coordinates.zCoord / 16.0)
        };

        if (string.isEmpty()) {
            String allCoordinates = roundedCoordinates[0] + " " + roundedCoordinates[1];
            try {
                this.parse(new StringReader(allCoordinates));
                builder.suggest(String.valueOf(roundedCoordinates[0]));
                builder.suggest(roundedCoordinates[0] + " " + roundedCoordinates[1]);
                builder.suggest(allCoordinates);
            } catch (CommandSyntaxException ignored) {}
        } else {
            String[] strings = string.split(" ");
            String allCoordinates;
            if (strings.length == 1) {
                allCoordinates = strings[0] + " " + roundedCoordinates[1];
                try {
                    this.parse(new StringReader(allCoordinates));
                    builder.suggest(strings[0] + " " + roundedCoordinates[1]);
                    builder.suggest(allCoordinates);
                } catch (CommandSyntaxException ignored) {}
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
