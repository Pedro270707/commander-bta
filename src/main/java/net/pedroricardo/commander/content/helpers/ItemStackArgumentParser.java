package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ItemStackArgumentParser extends ArgumentParser {
    private static final SimpleCommandExceptionType INVALID_ITEM = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("argument_types.commander.item.invalid_item"));

    private Item item;
    private int metadata = 0;
    private CompoundTag tag = new CompoundTag();

    public ItemStackArgumentParser(StringReader reader) {
        super(reader);
    }

    private CompletableFuture<Suggestions> suggestItems(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(this.startPosition);
        consumer.accept(suggestionsBuilder2);
        return suggestionsBuilder.add(suggestionsBuilder2).buildFuture();
    }
    
    public ItemStack parse() throws CommandSyntaxException {
        this.startPosition = this.reader.getCursor();

        this.suggestions = this::suggestItems;
        this.parseItem();

        if (this.item == null) throw INVALID_ITEM.createWithContext(this.reader);

        this.suggestions = this::suggestOpenMetadataOrTag;
        if (this.reader.canRead() && this.reader.peek() == '[') {
            this.metadata = this.parseMetadata();
        }

        if (this.reader.canRead() && this.reader.peek() == '{') {
            this.tag = this.parseCompound();
        }

        System.out.println(this.tag);
        if (this.item != null) {
            return new ItemStack(this.item, 1, this.metadata, this.tag);
        }
        throw INVALID_ITEM.createWithContext(this.reader);
    }

    private void parseItem() throws CommandSyntaxException {
        String string = this.reader.readString();
        for (Item itemInList : Item.itemsList) {
            if (itemInList == null) continue;
            if (CommanderHelper.matchesKeyString(itemInList.getKey(), string)) {
                this.item = itemInList;
            }
        }
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }
}
