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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlockArgumentType implements ArgumentType<Block> {
    private static final List<String> EXAMPLES = Arrays.asList("tile.stone", "stone", "dirt");

    public static ArgumentType<Block> block() {
        return new BlockArgumentType();
    }

    @Override
    public Block parse(StringReader reader) throws CommandSyntaxException {
        final String string = reader.readString();

        for (Block block : Block.blocksList) {
            if (block == null) continue;
            if (block.getKey().equals(string) || (block.getKey().startsWith("tile.") && block.getKey().substring("tile.".length()).equals(string))) {
                return block;
            }
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
