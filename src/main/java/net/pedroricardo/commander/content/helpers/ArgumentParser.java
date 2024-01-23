package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.nbt.*;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.NbtHelper;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class ArgumentParser {
    protected StringReader reader;
    protected int startPosition = 0;
    protected BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = CommanderHelper.NO_SUGGESTIONS;

    protected ArgumentParser(StringReader reader) {
        this.reader = reader;
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestions.apply(suggestionsBuilder.createOffset(this.reader.getCursor()), consumer);
    }

    protected int parseMetadata() throws CommandSyntaxException {
        this.suggestions = CommanderHelper.NO_SUGGESTIONS;
        this.reader.skip();
        this.reader.skipWhitespace();
        int cursor = this.reader.getCursor();
        int metadata = this.reader.readInt();
        if (metadata < 0) {
            this.reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(this.reader, metadata, 0);
        }
        this.reader.skipWhitespace();
        this.suggestions = this::suggestCloseMetadata;
        if (this.reader.canRead() && this.reader.peek() == ']') {
            this.suggestions = this::suggestOpenTag;
            this.reader.skip();
            return metadata;
        } else {
            throw CommanderExceptions.expectedEndOfMetadata().createWithContext(this.reader);
        }
    }

    protected CompoundTag parseCompound() throws CommandSyntaxException {
        return NbtHelper.parseNbt(this.reader);
    }

    protected CompletableFuture<Suggestions> suggestOpenMetadataOrTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) {
            suggestionsBuilder.suggest(String.valueOf('['));
            suggestionsBuilder.suggest(String.valueOf('{'));
        }
        return suggestionsBuilder.buildFuture();
    }

    protected CompletableFuture<Suggestions> suggestOpenTag(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf('{'));
        return suggestionsBuilder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestCloseMetadata(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer) {
        if (!this.reader.canRead()) suggestionsBuilder.suggest(String.valueOf(']'));
        return suggestionsBuilder.buildFuture();
    }
}
