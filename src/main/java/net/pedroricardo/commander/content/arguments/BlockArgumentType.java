package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.collection.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlockArgumentType implements ArgumentType<Pair<Block, Integer>> {
    private static final List<String> EXAMPLES = Arrays.asList("tile.stone", "stone", "dirt");

    public static ArgumentType<Pair<Block, Integer>> block() {
        return new BlockArgumentType();
    }

    @Override
    public Pair<Block, Integer> parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        Block block = null;
        for (Block blockInList : Block.blocksList) {
            if (blockInList == null) continue;
            if (blockInList.getKey().equals(string) || (blockInList.getKey().startsWith("tile.") && blockInList.getKey().substring("tile.".length()).equals(string))) {
                block = blockInList;
            }
        }
        if (reader.canRead() && reader.peek() == '[') {
            reader.skip();
            int cursor = reader.getCursor();
            int metadata = reader.readInt();
            if (metadata < 0) {
                reader.setCursor(cursor);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, metadata, 0);
            } else if (metadata > 255) {
                reader.setCursor(cursor);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, metadata, 255);
            }
            if (reader.canRead() && reader.peek() == ']') {
                reader.skip();
                return Pair.of(block, metadata);
            }
        } else if ((!reader.canRead() || reader.canRead() && reader.peek() == ' ') && block != null) {
            return Pair.of(block, 0);
        }
        throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), () -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_block"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Block block : Block.blocksList) {
            if (block == null) continue;
            if ("tile.".startsWith(remaining) || remaining.startsWith("tile.")) {
                if (block.getKey().toLowerCase().startsWith(remaining)) {
                    builder.suggest(block.getKey(), () -> I18n.getInstance().translateKey(block.getLanguageKey(0) + ".name"));
                }
            } else {
                if (block.getKey().startsWith("tile.") && block.getKey().substring("tile.".length()).toLowerCase().startsWith(remaining)) {
                    builder.suggest(block.getKey().substring("tile.".length()), () -> I18n.getInstance().translateKey(block.getLanguageKey(0) + ".name"));
                }
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
