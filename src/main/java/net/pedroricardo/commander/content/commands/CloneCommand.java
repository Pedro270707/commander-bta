package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.data.tag.Tag;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.CommanderHelper;
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

public class CloneCommand {
    private static final SimpleCommandExceptionType INSIDE_CLONED_AREA = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_inside_cloned_area"));
    private static final SimpleCommandExceptionType DESTINATION_NOT_LOADED = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_destination_not_loaded"));
    private static final SimpleCommandExceptionType SOURCE_NOT_LOADED = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_source_not_loaded"));
    private static final SimpleCommandExceptionType NOWHERE_LOADED = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clone.exception_nowhere_loaded"));

    public static void register(CommandDispatcher<CommanderCommandSource> commandDispatcher) {
        commandDispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("clone")
                .requires(CommanderCommandSource::hasAdmin)
                .then(beginEndDestinationAndModeSuffix(c -> {
                    return c.getSource().getWorld();
                }))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("from")
                        .then((RequiredArgumentBuilder.<CommanderCommandSource, Dimension>argument("sourceDimension", DimensionArgumentType.dimension()))
                                .then(beginEndDestinationAndModeSuffix(c -> {
                                    return c.getSource().getWorld(c.getArgument("sourceDimension", Dimension.class).id);
                                })))));
    }

    private static ArgumentBuilder<CommanderCommandSource, ?> beginEndDestinationAndModeSuffix(CommandFunction<CommandContext<CommanderCommandSource>, World> commandFunction) {
        return RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("begin", IntegerCoordinatesArgumentType.intCoordinates())
                .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("end", IntegerCoordinatesArgumentType.intCoordinates())
                        .then(destinationAndModeSuffix(commandFunction, c -> {
                            return c.getSource().getWorld();
                        }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("to")
                                .then((RequiredArgumentBuilder.<CommanderCommandSource, Dimension>argument("targetDimension", DimensionArgumentType.dimension()))
                                        .then(destinationAndModeSuffix(commandFunction, c -> {
                                            return c.getSource().getWorld(c.getArgument("targetDimension", Dimension.class).id);
                                        })))));
    }

    private static WorldAndPosition getWorldAndPosition(CommandContext<CommanderCommandSource> commandContext, World world, String string) {
        IntegerCoordinates coordinates = commandContext.getArgument(string, IntegerCoordinates.class);
        return new WorldAndPosition(world, coordinates);
    }

    private static ArgumentBuilder<CommanderCommandSource, ?> destinationAndModeSuffix(CommandFunction<CommandContext<CommanderCommandSource>, World> commandFunction, CommandFunction<CommandContext<CommanderCommandSource>, World> commandFunction2) {
        CommandFunction<CommandContext<CommanderCommandSource>, WorldAndPosition> commandFunction3 = c -> getWorldAndPosition(c, commandFunction.apply(c), "begin");
        CommandFunction<CommandContext<CommanderCommandSource>, WorldAndPosition> commandFunction4 = c -> getWorldAndPosition(c, commandFunction.apply(c), "end");
        CommandFunction<CommandContext<CommanderCommandSource>, WorldAndPosition> commandFunction5 = c -> getWorldAndPosition(c, commandFunction2.apply(c), "destination");
        return RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("destination", IntegerCoordinatesArgumentType.intCoordinates())
                .executes(c -> {
                    return clone(c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), null, CloneMode.NORMAL);
                })
                .then(wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, (commandContext) -> null, LiteralArgumentBuilder.<CommanderCommandSource>literal("replace")
                        .executes(c -> {
                            return clone(c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), null, CloneMode.NORMAL);
                        })))
                .then(wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> new BlockInput(null, 0, new CompoundTag()), LiteralArgumentBuilder.<CommanderCommandSource>literal("masked")
                        .executes(c -> {
                            return clone(c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), new BlockInput(null, 0, new CompoundTag()), CloneMode.NORMAL);
                        })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("filtered")
                        .then(wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, c -> {
                            return c.getArgument("filter", BlockInput.class);
                            }, RequiredArgumentBuilder.<CommanderCommandSource, BlockInput>argument("filter", BlockArgumentType.block())
                                .executes(c -> {
                                    return clone(c.getSource(), commandFunction3.apply(c), commandFunction4.apply(c), commandFunction5.apply(c), c.getArgument("filter", BlockInput.class), CloneMode.NORMAL);
                                }))));
    }

    private static ArgumentBuilder<CommanderCommandSource, ?> wrapWithCloneMode(CommandFunction<CommandContext<CommanderCommandSource>, WorldAndPosition> commandFunction, CommandFunction<CommandContext<CommanderCommandSource>, WorldAndPosition> commandFunction2, CommandFunction<CommandContext<CommanderCommandSource>, WorldAndPosition> commandFunction3, CommandFunction<CommandContext<CommanderCommandSource>, BlockInput> commandFunction4, ArgumentBuilder<CommanderCommandSource, ?> argumentBuilder) {
        return argumentBuilder.then(LiteralArgumentBuilder.<CommanderCommandSource>literal("force")
                .executes(c -> {
                    return clone(c.getSource(), commandFunction.apply(c), commandFunction2.apply(c), commandFunction3.apply(c), commandFunction4.apply(c), CloneMode.FORCE);
                }))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("move")
                        .executes(c -> {
                            return clone(c.getSource(), commandFunction.apply(c), commandFunction2.apply(c), commandFunction3.apply(c), commandFunction4.apply(c), CloneMode.MOVE);
                        }))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("normal")
                        .executes(c -> {
                            return clone(c.getSource(), commandFunction.apply(c), commandFunction2.apply(c), commandFunction3.apply(c), commandFunction4.apply(c), CloneMode.NORMAL);
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
            CompoundTag blockTag = new CompoundTag();
            TileEntity tileEntity = destination.getWorld().getBlockTileEntity(offsetDestinationX, offsetDestinationY, offsetDestinationZ);
            if (tileEntity != null) tileEntity.writeToNBT(blockTag);
            if (filter == null || (destination.getWorld().getBlockId(offsetDestinationX, offsetDestinationY, offsetDestinationZ) == filter.getBlockId() && destination.getWorld().getBlockMetadata(offsetDestinationX, offsetDestinationY, offsetDestinationZ) == filter.getMetadata() && (filter.getTag().getValues().isEmpty() || CommanderHelper.blockEntitiesAreEqual(blockTag, filter.getTag())))) {
                if (destination.getWorld().getBlockId(offsetDestinationX, offsetDestinationY, offsetDestinationZ) != entry.getValue().getBlockId() || destination.getWorld().getBlockMetadata(offsetDestinationX, offsetDestinationY, offsetDestinationZ) != entry.getValue().getMetadata()) ++clonedBlocks;
                destination.getWorld().setBlockWithNotify(offsetDestinationX, offsetDestinationY, offsetDestinationZ, entry.getValue().getBlockId());
                destination.getWorld().setBlockMetadataWithNotify(offsetDestinationX, offsetDestinationY, offsetDestinationZ, entry.getValue().getMetadata());
                if (entry.getValue().getTileEntity() != null) {
                    CommanderHelper.setTileEntity(destination.getWorld(), offsetDestinationX, offsetDestinationY, offsetDestinationZ, entry.getValue().getTileEntity());
                }
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
