package net.pedroricardo.commander.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.core.net.packet.Packet;
import net.pedroricardo.commander.duck.CommandManagerPacketHandler;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandManagerPacket extends Packet {
    private CommandDispatcher<CommanderCommandSource> dispatcher;
    private CommanderCommandSource source;
    private String text;
    private int cursor;

    public JsonObject suggestions;

    public CommandManagerPacket(CommandDispatcher<CommanderCommandSource> dispatcher, CommanderCommandSource source, String text, int cursor) {
        this.dispatcher = dispatcher;
        this.source = source;
        this.text = text;
        this.cursor = cursor;
    }

    public CommandManagerPacket() {
    }

    @Override
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        String str = dataInputStream.readUTF();
        this.suggestions = JsonParser.parseString(str).getAsJsonObject();
    }

    @Override
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        String str = getDispatcherSuggestions(this.dispatcher, this.source, this.text, this.cursor).toString();
        dataOutputStream.writeUTF(str);
    }

    private static JsonObject getDispatcherSuggestions(CommandDispatcher<CommanderCommandSource> dispatcher, CommanderCommandSource source, String text, int cursor) {
        final JsonObject object = new JsonObject();
        final JsonArray suggestions = new JsonArray();
        final JsonArray exceptions = new JsonArray();
        final JsonArray usage = new JsonArray();
        StringReader reader = new StringReader(text);
        if (cursor >= 1 && reader.canRead() && reader.read() == '/') {
            ParseResults<CommanderCommandSource> parseResults = dispatcher.parse(reader, source);
            JsonObject readerJson = new JsonObject();
            readerJson.addProperty(CommandManagerPacketKeys.READER_CAN_READ, parseResults.getReader().canRead());
            int readerCursor = Math.max(parseResults.getReader().getCursor(), 0);
            readerJson.addProperty(CommandManagerPacketKeys.READER_CURSOR, readerCursor);
            int remainingTextLength = Math.min(readerCursor + parseResults.getReader().getRemainingLength(), text.length());
            readerJson.addProperty(CommandManagerPacketKeys.READER_REMAINING_TEXT_LENGTH, remainingTextLength);
            readerJson.addProperty(CommandManagerPacketKeys.READER_STRING, parseResults.getReader().getString());
            object.add(CommandManagerPacketKeys.READER, readerJson);

            CompletableFuture<Suggestions> pendingSuggestions = getCompletionSuggestions(parseResults, cursor, source);
            pendingSuggestions.thenRun(() -> {
                if (pendingSuggestions.isDone()) {
                    for (Suggestion suggestion : pendingSuggestions.join().getList()) {
                        JsonObject suggestionJson = new JsonObject();
                        suggestionJson.addProperty(CommandManagerPacketKeys.VALUE, suggestion.getText());
                        JsonObject range = new JsonObject();
                        range.addProperty(CommandManagerPacketKeys.RANGE_START, suggestion.getRange().getStart());
                        range.addProperty(CommandManagerPacketKeys.RANGE_END, suggestion.getRange().getEnd());
                        suggestionJson.add(CommandManagerPacketKeys.RANGE, range);
                        if (suggestion.getTooltip() != null)
                            suggestionJson.addProperty(CommandManagerPacketKeys.TOOLTIP, suggestion.getTooltip().getString());
                        suggestions.add(suggestionJson);
                    }
                }
            });
            CommandSyntaxException parseException = CommanderCommandManager.getParseException(parseResults);
            if (!parseResults.getExceptions().isEmpty()) {
                for (CommandSyntaxException entry : parseResults.getExceptions().values()) {
                    JsonObject exceptionJson = new JsonObject();
                    exceptionJson.addProperty(CommandManagerPacketKeys.VALUE, entry.getMessage());
                    exceptions.add(exceptionJson);
                }
            } else if (parseException != null) {
                JsonObject exceptionJson = new JsonObject();
                exceptionJson.addProperty(CommandManagerPacketKeys.VALUE, parseException.getMessage());
                exceptions.add(exceptionJson);
            } else if (parseResults.getContext().getRootNode() != null && parseResults.getContext().getRange().getStart() <= cursor) {
                JsonObject commandUsage = new JsonObject();
                for (Map.Entry<CommandNode<CommanderCommandSource>, String> entry : dispatcher.getSmartUsage(parseResults.getContext().findSuggestionContext(cursor).parent, source).entrySet()) {
                    if (entry.getKey() instanceof LiteralCommandNode) continue;
                    commandUsage.addProperty(CommandManagerPacketKeys.VALUE, entry.getValue());
                    usage.add(commandUsage);
                }
            }

            JsonObject lastChild = getLastChild(parseResults);
            object.add(CommandManagerPacketKeys.LAST_CHILD, lastChild);
        }
        object.add(CommandManagerPacketKeys.SUGGESTIONS, suggestions);
        object.add(CommandManagerPacketKeys.EXCEPTIONS, exceptions);
        object.add(CommandManagerPacketKeys.USAGE, usage);
        return object;
    }

    private static CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<CommanderCommandSource> parse, int cursor, CommanderCommandSource source) {
        final CommandContextBuilder<CommanderCommandSource> context = parse.getContext();

        final SuggestionContext<CommanderCommandSource> nodeBeforeCursor = context.findSuggestionContext(cursor);
        final CommandNode<CommanderCommandSource> parent = nodeBeforeCursor.parent;
        if (parent == null) return Suggestions.empty();
        final int start = Math.min(nodeBeforeCursor.startPos, cursor);

        final String fullInput = parse.getReader().getString();
        final String truncatedInput = fullInput.substring(0, cursor);
        final String truncatedInputLowerCase = truncatedInput.toLowerCase(Locale.ROOT);
        List<CompletableFuture<Suggestions>> futuresList = new ArrayList<>();
        int i = 0;
        for (final CommandNode<CommanderCommandSource> node : parent.getChildren()) {
            if (!node.canUse(source)) continue;
            CompletableFuture<Suggestions> future = Suggestions.empty();
            try {
                future = node.listSuggestions(context.build(truncatedInput), new SuggestionsBuilder(truncatedInput, truncatedInputLowerCase, start));
            } catch (final CommandSyntaxException ignored) {
            }
            futuresList.add(future);
            i++;
        }

        @SuppressWarnings("unchecked") final CompletableFuture<Suggestions>[] futures = new CompletableFuture[i];
        for (int j = 0; j < futures.length; j++) {
            futures[j] = futuresList.get(j);
        }

        final CompletableFuture<Suggestions> result = new CompletableFuture<>();
        CompletableFuture.allOf(futures).thenRun(() -> {
            final List<Suggestions> suggestions = new ArrayList<>();
            for (final CompletableFuture<Suggestions> future : futuresList) {
                suggestions.add(future.join());
            }
            result.complete(Suggestions.merge(fullInput, suggestions));
        });

        return result;
    }

    @NotNull
    private static JsonObject getLastChild(ParseResults<CommanderCommandSource> parseResults) {
        JsonObject lastChild = new JsonObject();
        CommandContextBuilder<CommanderCommandSource> builder = parseResults.getContext().getLastChild();
        JsonArray arguments = new JsonArray();
        for (ParsedArgument<CommanderCommandSource, ?> parsedArgument : builder.getArguments().values()) {
            JsonObject argument = new JsonObject();
            JsonObject range = new JsonObject();
            range.addProperty(CommandManagerPacketKeys.RANGE_START, parsedArgument.getRange().getStart());
            range.addProperty(CommandManagerPacketKeys.RANGE_END, parsedArgument.getRange().getEnd());
            argument.add(CommandManagerPacketKeys.RANGE, range);
            arguments.add(argument);
        }
        lastChild.add(CommandManagerPacketKeys.ARGUMENTS, arguments);
        return lastChild;
    }

//    private static JsonObject getDispatcherSuggestions(CommandDispatcher<CommanderCommandSource> dispatcher, CommanderCommandSource source) {
//        JsonObject object = new JsonObject();
//        JsonArray array = new JsonArray();
//        for (CommandNode<CommanderCommandSource> node : dispatcher.getRoot().getChildren()) {
//            JsonObject parsedNode = parseNode(node, source);
//            if (!parsedNode.isEmpty()) array.add(parsedNode);
//        }
//        object.add("values", array);
//        return object;
//    }
//
//    private static JsonObject parseNode(CommandNode<CommanderCommandSource> node, CommanderCommandSource source) {
//        System.out.println("Parsing node");
//        JsonObject object = new JsonObject();
//        if (node.canUse(source)) {
//            object.addProperty(node instanceof LiteralCommandNode ? "literal" : "required", node.getName());
//            if (!node.getChildren().isEmpty()) {
//                JsonArray children = new JsonArray();
//                for (CommandNode<CommanderCommandSource> child : node.getChildren()) {
//                    JsonObject parsedNode = parseNode(child, source);
//                    if (!parsedNode.isEmpty()) children.add(parsedNode);
//                }
//                object.add("children", children);
//            }
//        }
//        return object;
//    }

    @Override
    public void processPacket(NetHandler netHandler) {
        ((CommandManagerPacketHandler)netHandler).commander$handleCommandManagerPacket(this);
    }

    @Override
    public int getPacketSize() {
        return 1;
    }
}
