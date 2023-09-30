package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BlockArgumentParser extends ArgumentParser {
    private static final SimpleCommandExceptionType INVALID_BLOCK = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.block.invalid_block"));

    private Block block;
    private int metadata = 0;
    private CompoundTag tag = new CompoundTag();

    public BlockArgumentParser(StringReader reader) {
        super(reader);
    }

    private CompletableFuture<Suggestions> suggestBlocks(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(this.startPosition);
        consumer.accept(suggestionsBuilder2);
        return suggestionsBuilder.add(suggestionsBuilder2).buildFuture();
    }

    public BlockInput parse() throws CommandSyntaxException {
        this.startPosition = this.reader.getCursor();

        this.suggestions = this::suggestBlocks;
        this.parseBlock();

        if (this.block == null) throw INVALID_BLOCK.createWithContext(this.reader);

        this.suggestions = this::suggestOpenMetadataOrTag;
        if (this.reader.canRead() && this.reader.peek() == '[') {
            this.metadata = this.parseMetadata();
        }

        if (this.reader.canRead() && this.reader.peek() == '{') {
            this.tag = this.parseTag();
        }

        if (this.block != null) {
            return new BlockInput(this.block, this.metadata, this.tag);
        }
        throw INVALID_BLOCK.createWithContext(this.reader);
    }

    private void parseBlock() throws CommandSyntaxException {
        String string = this.reader.readString();
        for (Block blockInList : Block.blocksList) {
            if (blockInList == null) continue;
            if (CommanderHelper.matchesKeyString(blockInList.getKey(), string)) {
                this.block = blockInList;
            }
        }
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }
}
