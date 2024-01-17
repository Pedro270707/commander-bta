package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BlockArgumentType;
import net.pedroricardo.commander.content.arguments.DimensionArgumentType;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.BlockInput;
import net.pedroricardo.commander.content.helpers.ClonedBlock;
import net.pedroricardo.commander.content.helpers.IntegerCoordinate;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CloneCommand {
    private static final SimpleCommandExceptionType INSIDE_CLONED_AREA = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_inside_cloned_area"));
    private static final SimpleCommandExceptionType DESTINATION_NOT_LOADED = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_destination_not_loaded"));
    private static final SimpleCommandExceptionType SOURCE_NOT_LOADED = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_source_not_loaded"));
    private static final SimpleCommandExceptionType NOWHERE_LOADED = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_nowhere_loaded"));

//    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
//        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("clone")
//                .then(LiteralArgumentBuilder.literal("from")
//                        .then(RequiredArgumentBuilder.argument("sourceDimension", DimensionArgumentType.dimension())
//                                .then(RequiredArgumentBuilder.argument("begin", IntegerCoordinatesArgumentType.intCoordinates())
//                                        .then(RequiredArgumentBuilder.argument("end", IntegerCoordinatesArgumentType.intCoordinates())
//                                                .then(RequiredArgumentBuilder.argument("destination", IntegerCoordinatesArgumentType.intCoordinates())
//                                                        .executes(c -> {
//                                                            IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                            IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                            IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                            Dimension sourceDimension = c.getArgument("sourceDimension", Dimension.class);
//                                                            if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                            int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(sourceDimension.id), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL);
//                                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                            return clonedBlocks;
//                                                        })
//                                                        .then(LiteralArgumentBuilder.literal("filtered")
//                                                                .then(RequiredArgumentBuilder.argument("filter", BlockArgumentType.block())
//                                                                        .executes(c -> {
//                                                                            IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                                            IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                                            IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                                            Dimension sourceDimension = c.getArgument("sourceDimension", Dimension.class);
//                                                                            BlockInput filter = c.getArgument("filter", BlockInput.class);
//                                                                            if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                                            int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(sourceDimension.id), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL, filter);
//                                                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                                            return clonedBlocks;
//                                                                        })))
//                                                        .then(LiteralArgumentBuilder.literal("masked")
//                                                                .executes(c -> {
//                                                                    IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                                    IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                                    IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                                    if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                                    int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL, new BlockInput(null, 0, new CompoundTag()));
//                                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                                    return clonedBlocks;
//                                                                })))
//                                                .then(LiteralArgumentBuilder.literal("to")
//                                                        .then(RequiredArgumentBuilder.argument("targetDimension", DimensionArgumentType.dimension())
//                                                                .then(RequiredArgumentBuilder.argument("destination", IntegerCoordinatesArgumentType.intCoordinates())
//                                                                        .executes(c -> {
//                                                                            IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                                            IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                                            IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                                            Dimension sourceDimension = c.getArgument("sourceDimension", Dimension.class);
//                                                                            Dimension targetDimension = c.getArgument("targetDimension", Dimension.class);
//                                                                            if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                                            int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(sourceDimension.id), ((CommanderCommandSource) c.getSource()).getWorld(targetDimension.id), begin, end, destination, CloneMode.NORMAL);
//                                                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                                            return clonedBlocks;
//                                                                        })
//                                                                        .then(LiteralArgumentBuilder.literal("filtered")
//                                                                                .then(RequiredArgumentBuilder.argument("filter", BlockArgumentType.block())
//                                                                                        .executes(c -> {
//                                                                                            IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                                                            IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                                                            IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                                                            Dimension sourceDimension = c.getArgument("sourceDimension", Dimension.class);
//                                                                                            Dimension targetDimension = c.getArgument("targetDimension", Dimension.class);
//                                                                                            BlockInput filter = c.getArgument("filter", BlockInput.class);
//                                                                                            if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                                                            int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(sourceDimension.id), ((CommanderCommandSource) c.getSource()).getWorld(targetDimension.id), begin, end, destination, CloneMode.NORMAL, filter);
//                                                                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                                                            return clonedBlocks;
//                                                                                        })))
//                                                                        .then(LiteralArgumentBuilder.literal("masked")
//                                                                                .executes(c -> {
//                                                                                    IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                                                    IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                                                    IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                                                    if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                                                    int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL, new BlockInput(null, 0, new CompoundTag()));
//                                                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                                                    return clonedBlocks;
//                                                                                })))))))))
//                .then(RequiredArgumentBuilder.argument("begin", IntegerCoordinatesArgumentType.intCoordinates())
//                        .then(RequiredArgumentBuilder.argument("end", IntegerCoordinatesArgumentType.intCoordinates())
//                                .then(RequiredArgumentBuilder.argument("destination", IntegerCoordinatesArgumentType.intCoordinates())
//                                        .executes(c -> {
//                                            IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                            IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                            IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                            if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                            int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL);
//                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                            return clonedBlocks;
//                                        })
//                                        .then(LiteralArgumentBuilder.literal("filtered")
//                                                .then(RequiredArgumentBuilder.argument("filter", BlockArgumentType.block())
//                                                        .executes(c -> {
//                                                            IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                            IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                            IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                            BlockInput filter = c.getArgument("filter", BlockInput.class);
//                                                            if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                            int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL, filter);
//                                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                            return clonedBlocks;
//                                                        })))
//                                        .then(LiteralArgumentBuilder.literal("masked")
//                                                .executes(c -> {
//                                                    IntegerCoordinates begin = c.getArgument("begin", IntegerCoordinates.class);
//                                                    IntegerCoordinates end = c.getArgument("end", IntegerCoordinates.class);
//                                                    IntegerCoordinates destination = c.getArgument("destination", IntegerCoordinates.class);
//                                                    if (CommanderHelper.getVolume((CommanderCommandSource)c.getSource(), begin, end) > 32768) throw CommanderExceptions.volumeTooLarge().create();
//                                                    int clonedBlocks = clone((CommanderCommandSource) c.getSource(), ((CommanderCommandSource) c.getSource()).getWorld(), ((CommanderCommandSource) c.getSource()).getWorld(), begin, end, destination, CloneMode.NORMAL, new BlockInput(null, 0, new CompoundTag()));
//                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
//                                                    return clonedBlocks;
//                                                }))))));
//    }

    public static void register(CommandDispatcher<CommanderCommandSource> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("clone")
                .requires((source) -> ((CommanderCommandSource)source).hasAdmin()))
                .then(beginEndDestinationAndModeSuffix(c -> {
                    return c.getSource().getWorld();
                })))
                .then(LiteralArgumentBuilder.literal("from")
                        .then(((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("sourceDimension", DimensionArgumentType.dimension()))
                                .then(beginEndDestinationAndModeSuffix(c -> {
                                    return c.getSource().getWorld(c.getArgument("sourceDimension", Dimension.class).id);
                                })))));
    }

    private static ArgumentBuilder<CommanderCommandSource, ?> beginEndDestinationAndModeSuffix(CommandFunction<CommandContext<CommanderCommandSource>, World> commandFunction) {
        return RequiredArgumentBuilder.argument("begin", IntegerCoordinatesArgumentType.intCoordinates())
                .then((((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("end", IntegerCoordinatesArgumentType.intCoordinates()))
                        .then(destinationAndModeSuffix(commandFunction, c -> {
                            return c.getSource().getWorld();
                        })))
                        .then(LiteralArgumentBuilder.literal("to")
                                .then(((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("targetDimension", DimensionArgumentType.dimension()))
                                        .then(destinationAndModeSuffix(commandFunction, c -> {
                                            return c.getSource().getWorld(c.getArgument("targetDimension", Dimension.class).id);
                                        })))));
    }

    private static WorldAndPosition getWorldAndPosition(CommandContext<CommanderCommandSource> commandContext, World world, String string) {
        IntegerCoordinates coordinates = commandContext.getArgument(string, IntegerCoordinates.class);
        return new WorldAndPosition(world, coordinates);
    }

    private static ArgumentBuilder<CommanderCommandSource, ?> destinationAndModeSuffix(CommandFunction<CommandContext<CommanderCommandSource>, World> commandFunction, CommandFunction<CommandContext<CommanderCommandSource>, World> commandFunction2) {
        CommandFunction<CommandContext, WorldAndPosition> commandFunction3 = c -> getWorldAndPosition(c, commandFunction.apply(c), "begin");
        CommandFunction<CommandContext, WorldAndPosition> commandFunction4 = c -> getWorldAndPosition(c, commandFunction.apply(c), "end");
        CommandFunction<CommandContext, WorldAndPosition> commandFunction5 = c -> getWorldAndPosition(c, commandFunction2.apply(c), "destination");
        return ((((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("destination", IntegerCoordinatesArgumentType.intCoordinates())
                .executes(c -> {
                    return clone((CommanderCommandSource)c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), null, CloneMode.NORMAL);
                }))
                .then(wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, (commandContext) -> null, ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("replace"))
                        .executes(c -> {
                            return clone((CommanderCommandSource)c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), null, CloneMode.NORMAL);
                        }))))
                .then(wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> new BlockInput(null, 0, new CompoundTag()), ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("masked"))
                        .executes(c -> {
                            return clone((CommanderCommandSource)c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), new BlockInput(null, 0, new CompoundTag()), CloneMode.NORMAL);
                        }))))
                .then(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("filtered"))
                        .then(wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, c -> {
                            return (BlockInput) c.getArgument("filter", BlockInput.class);
                            }, RequiredArgumentBuilder.argument("filter", BlockArgumentType.block())
                                .executes(c -> {
                                    return clone((CommanderCommandSource)c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), c.getArgument("filter", BlockInput.class), CloneMode.NORMAL);
                                }))));
    }

    private static ArgumentBuilder wrapWithCloneMode(CommandFunction<CommandContext, WorldAndPosition> commandFunction, CommandFunction<CommandContext, WorldAndPosition> commandFunction2, CommandFunction<CommandContext, WorldAndPosition> commandFunction3, CommandFunction<CommandContext, BlockInput> commandFunction4, ArgumentBuilder argumentBuilder) {
        return argumentBuilder.then(LiteralArgumentBuilder.literal("force")
                .executes(c -> {
                    return clone((CommanderCommandSource)c.getSource(), commandFunction.apply(c), commandFunction2.apply(c), commandFunction3.apply(c), commandFunction4.apply(c), CloneMode.FORCE);
                }))
                .then(LiteralArgumentBuilder.literal("move")
                        .executes(c -> {
                            return clone((CommanderCommandSource)c.getSource(), commandFunction.apply(c), commandFunction2.apply(c), commandFunction3.apply(c), commandFunction4.apply(c), CloneMode.MOVE);
                        }))
                .then(LiteralArgumentBuilder.literal("normal")
                        .executes(c -> {
                            return clone((CommanderCommandSource)c.getSource(), commandFunction.apply(c), commandFunction2.apply(c), commandFunction3.apply(c), commandFunction4.apply(c), CloneMode.NORMAL);
                        }));
    }

    @FunctionalInterface
    interface CommandFunction<T, R> {
        R apply(T object) throws CommandSyntaxException;
    }

    public static class WorldAndPosition {
        private final World world;
        private final IntegerCoordinates position;

        WorldAndPosition(World world, IntegerCoordinates position) {
            this.world = world;
            this.position = position;
        }

        public World getWorld() {
            return this.world;
        }

        public IntegerCoordinates getPosition() {
            return this.position;
        }
    }

    public static int clone(CommanderCommandSource source, WorldAndPosition start, WorldAndPosition end, WorldAndPosition destination, @Nullable BlockInput filter, CloneMode cloneMode) throws CommandSyntaxException {
        int minX = Math.min(start.getPosition().getX(source), end.getPosition().getX(source));
        int minY = Math.min(start.getPosition().getY(source, true), end.getPosition().getY(source, true));
        int minZ = Math.min(start.getPosition().getZ(source), end.getPosition().getZ(source));
        int maxX = Math.max(start.getPosition().getX(source), end.getPosition().getX(source));
        int maxY = Math.max(start.getPosition().getY(source, true), end.getPosition().getY(source, true));
        int maxZ = Math.max(start.getPosition().getZ(source), end.getPosition().getZ(source));

        int destinationX = destination.getPosition().getX(source);
        int destinationY = destination.getPosition().getY(source, true);
        int destinationZ = destination.getPosition().getZ(source);

        if (cloneMode == CloneMode.NORMAL && new AABB(minX, minY, minZ, maxX, maxY, maxZ).isVecInside(Vec3d.createVector(destinationX, destinationY, destinationZ)) && start.getWorld() == destination.getWorld()) {
            throw INSIDE_CLONED_AREA.create();
        }

        start.getWorld().editingBlocks = true;
        Map<IntegerCoordinates, ClonedBlock> map = new HashMap<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!start.getWorld().isBlockLoaded(x, y, z)) {
                        if (!destination.getWorld().isBlockLoaded(destinationX, destinationY, destinationZ)) throw NOWHERE_LOADED.create();
                        else throw SOURCE_NOT_LOADED.create();
                    }
                    map.put(new IntegerCoordinates(new IntegerCoordinate(false, x - minX), new IntegerCoordinate(false, y - minY), new IntegerCoordinate(false, z - minZ)), new ClonedBlock(start.getWorld().getBlock(x, y, z), start.getWorld().getBlockMetadata(x, y, z), start.getWorld().getBlockTileEntity(x, y, z)));
                    if (cloneMode == CloneMode.MOVE) {
                        start.getWorld().setBlockWithNotify(x, y, z, 0);
                    }
                }
            }
        }
        if (!destination.getWorld().isBlockLoaded(destinationX, destinationY, destinationZ)) throw DESTINATION_NOT_LOADED.create();
        start.getWorld().editingBlocks = false;

        int clonedBlocks = 0;
        destination.getWorld().editingBlocks = true;
        for (Map.Entry<IntegerCoordinates, ClonedBlock> entry : map.entrySet()) {
            int offsetDestinationX = destinationX + entry.getKey().getX(source);
            int offsetDestinationY = destinationY + entry.getKey().getY(source, true);
            int offsetDestinationZ = destinationZ + entry.getKey().getZ(source);
            if (filter == null || (destination.getWorld().getBlockId(offsetDestinationX, offsetDestinationY, offsetDestinationZ) == filter.getBlockId() && destination.getWorld().getBlockMetadata(offsetDestinationX, offsetDestinationY, offsetDestinationZ) == filter.getMetadata())) {
                ++clonedBlocks;
                destination.getWorld().setBlockAndMetadataWithNotify(offsetDestinationX, offsetDestinationY, offsetDestinationZ, entry.getValue().getBlockId(), entry.getValue().getMetadata());
                if (entry.getValue().getTileEntity() != null) destination.getWorld().setBlockTileEntity(offsetDestinationX, offsetDestinationY, offsetDestinationZ, entry.getValue().getTileEntity());
            }
        }
        destination.getWorld().editingBlocks = false;
        source.sendTranslatableMessage(clonedBlocks == 1 ? "commands.commander.clone.success_single" : "commands.commander.clone.success_multiple", clonedBlocks);
        return clonedBlocks;
    }

    public enum CloneMode {
        FORCE,
        MOVE,
        NORMAL
    }
}
