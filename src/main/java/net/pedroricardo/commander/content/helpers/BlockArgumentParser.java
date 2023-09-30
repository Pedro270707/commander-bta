package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.collection.Pair;
import net.pedroricardo.commander.CommanderHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class BlockArgumentParser {
    private static final SimpleCommandExceptionType INVALID_BLOCK = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_block"));

    private final StringReader reader;
    private int startPosition = 0;

    private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = CommanderHelper.NO_SUGGESTIONS;

    public BlockArgumentParser(StringReader reader) {
        this.reader = reader;
    }

    private CompletableFuture<Suggestions> suggestBlocks(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(this.startPosition);
        consumer.accept(suggestionsBuilder2);
        return suggestionsBuilder.add(suggestionsBuilder2).buildFuture();
    }
    
    public Pair<Block, Integer> parse() throws CommandSyntaxException {
        this.startPosition = this.reader.getCursor();
        final String string = this.reader.readString();

        this.suggestions = this::suggestBlocks;
        Block block = null;
        for (Block blockInList : Block.blocksList) {
            if (blockInList == null) continue;
            if (CommanderHelper.matchesKeyString(blockInList.getKey(), string)) {
                block = blockInList;
            }
        }

        if (this.reader.canRead() && this.reader.peek() == '[' && block != null) {
            this.suggestions = CommanderHelper.NO_SUGGESTIONS;
            this.reader.skip();
            this.reader.skipWhitespace();
            int cursor = this.reader.getCursor();
            int metadata = this.reader.readInt();
            if (metadata < 0) {
                this.reader.setCursor(cursor);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(this.reader, metadata, 0);
            } else if (metadata > 255) {
                this.reader.setCursor(cursor);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(this.reader, metadata, 255);
            }
            this.reader.skipWhitespace();
            this.suggestions = this::suggestCloseMetadata;
            if (this.reader.canRead() && this.reader.peek() == ']') {
                this.suggestions = CommanderHelper.NO_SUGGESTIONS;
                this.reader.skip();
                return Pair.of(block, metadata);
            }
        } else if ((!this.reader.canRead() || this.reader.canRead() && this.reader.peek() == ' ') && block != null) {
            this.suggestions = this::suggestOpenMetadata;
            return Pair.of(block, 0);
        }
        this.reader.setCursor(this.startPosition);
        throw INVALID_BLOCK.createWithContext(this.reader);
    }

    private CompletableFuture<Suggestions> suggestOpenMetadata(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf('['));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCloseMetadata(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf(']'));
        return suggestionsBuilder.buildFuture();
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }
}
