package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.block.Block;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.helpers.BlockArgumentParser;
import net.pedroricardo.commander.content.helpers.BlockInput;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BlockArgumentType implements ArgumentType<BlockInput> {
    private static final List<String> EXAMPLES = Arrays.asList("tile.stone", "stone", "tile.log[1]");

    public static ArgumentType<BlockInput> block() {
        return new BlockArgumentType();
    }

    @Override
    public BlockInput parse(StringReader reader) throws CommandSyntaxException {
        BlockArgumentParser parser = new BlockArgumentParser(reader);
        return parser.parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        BlockArgumentParser parser = new BlockArgumentParser(stringReader);
        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {}
        return parser.fillSuggestions(builder, suggestionsBuilder -> {
            String remaining = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
            for (Block block : Block.blocksList) {
                if (block == null) continue;
                CommanderHelper.getStringToSuggest(block.getKey(), remaining).ifPresent(suggestionsBuilder::suggest);
            }
            suggestionsBuilder.buildFuture();
        });
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
