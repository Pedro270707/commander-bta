package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BlockArgumentType;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.BlockInput;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;
import org.jetbrains.annotations.Nullable;

public class FillCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("fill")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("first", IntegerCoordinatesArgumentType.intCoordinates())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("second", IntegerCoordinatesArgumentType.intCoordinates())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, BlockInput>argument("block", BlockArgumentType.block())
                                        .executes(c -> {
                                            IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                            IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                            BlockInput block = c.getArgument("block", BlockInput.class);
                                            if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                            int blocksFilled = fillReplace(c.getSource(), c.getSource().getWorld(), first, second, block);
                                            c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                            return blocksFilled;
                                        })
                                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("replace")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                                    int blocksFilled = fillReplace(c.getSource(), c.getSource().getWorld(), first, second, block);
                                                    c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                })
                                                .then(RequiredArgumentBuilder.<CommanderCommandSource, BlockInput>argument("filter", BlockArgumentType.block())
                                                        .executes(c -> {
                                                            IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                            IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                            BlockInput block = c.getArgument("block", BlockInput.class);
                                                            BlockInput filter = c.getArgument("filter", BlockInput.class);
                                                            if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                                            int blocksFilled = fillReplace(c.getSource(), c.getSource().getWorld(), first, second, block, filter);
                                                            c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                            return blocksFilled;
                                                        })))
                                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("hollow")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                                    int blocksFilled = fillHollow(c.getSource(), c.getSource().getWorld(), first, second, block);
                                                    c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))
                                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("outline")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                                    int blocksFilled = fillOutline(c.getSource(), c.getSource().getWorld(), first, second, block);
                                                    c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))
                                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("keep")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                                    int blocksFilled = fillKeep(c.getSource(), c.getSource().getWorld(), first, second, block);
                                                    c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))
                                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("destroy")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (CommanderHelper.getVolume(c.getSource(), first, second) > 32768) throw CommanderExceptions.volumeTooLarge().create();
                                                    int blocksFilled = fillDestroy(c.getSource(), c.getSource().getWorld(), first, second, block);
                                                    c.getSource().sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))))));
    }

    public static int fillReplace(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block) throws CommandSyntaxException {
        return fillReplace(source, world, first, second, block, null);
    }

    public static int fillReplace(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block, @Nullable BlockInput filter) throws CommandSyntaxException {
        return fillReplace(source, world, first, second, block, filter, false);
    }

    public static int fillReplace(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block, @Nullable BlockInput filter, boolean destroy) throws CommandSyntaxException {
        world.editingBlocks = true;
        int minX = Math.min(first.getX(source), second.getX(source));
        int minY = Math.min(first.getY(source, true), second.getY(source, true));
        int minZ = Math.min(first.getZ(source), second.getZ(source));
        int maxX = Math.max(first.getX(source), second.getX(source));
        int maxY = Math.max(first.getY(source, true), second.getY(source, true));
        int maxZ = Math.max(first.getZ(source), second.getZ(source));

        int blocksFilled = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    CompoundTag blockTag = new CompoundTag();
                    TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
                    if (tileEntity != null) tileEntity.writeToNBT(blockTag);
                    if ((block.getBlockId() != world.getBlockId(x, y, z) || block.getMetadata() != world.getBlockMetadata(x, y, z) || !CommanderHelper.blockEntitiesAreEqual(block.getTag(), CommanderHelper.tagFrom(world.getBlockTileEntity(x, y, z)))) && (filter == null || (world.getBlockId(x, y, z) == filter.getBlockId() && world.getBlockMetadata(x, y, z) == filter.getMetadata() && (filter.getTag().getValues().isEmpty() || CommanderHelper.blockEntitiesAreEqual(blockTag, filter.getTag()))))) {
                        ++blocksFilled;
                        if (destroy && world.getBlock(x, y, z) != null) world.getBlock(x, y, z).getBreakResult(world, EnumDropCause.WORLD, x, y, z, world.getBlockMetadata(x, y, z), world.getBlockTileEntity(x, y, z));
                    }
                    if (filter == null || (world.getBlockId(x, y, z) == filter.getBlockId() && world.getBlockMetadata(x, y, z) == filter.getMetadata() && (!filter.getTag().getValue().isEmpty() || CommanderHelper.blockEntitiesAreEqual(CommanderHelper.tagFrom(world.getBlockTileEntity(x, y, z)), filter.getTag())))) {
                        world.setBlockWithNotify(x, y, z, block.getBlockId());
                        world.setBlockMetadataWithNotify(x, y, z, block.getMetadata());
                    }
                }
            }
        }
        world.editingBlocks = false;

        return blocksFilled;
    }

    public static int fillHollow(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block) throws CommandSyntaxException {
        world.editingBlocks = true;
        int minX = Math.min(first.getX(source), second.getX(source));
        int minY = Math.min(first.getY(source, true), second.getY(source, true));
        int minZ = Math.min(first.getZ(source), second.getZ(source));
        int maxX = Math.max(first.getX(source), second.getX(source));
        int maxY = Math.max(first.getY(source, true), second.getY(source, true));
        int maxZ = Math.max(first.getZ(source), second.getZ(source));

        int blocksFilled = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isOutline = x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
                    if ((isOutline && (block.getBlockId() != world.getBlockId(x, y, z) || block.getMetadata() != world.getBlockMetadata(x, y, z) || !CommanderHelper.blockEntitiesAreEqual(block.getTag(), CommanderHelper.tagFrom(world.getBlockTileEntity(x, y, z))))) || (!isOutline && world.getBlockId(x, y, z) != 0)) {
                        ++blocksFilled;
                        if (!isOutline && world.getBlock(x, y, z) != null) world.getBlock(x, y, z).getBreakResult(world, EnumDropCause.WORLD, x, y, z, world.getBlockMetadata(x, y, z), world.getBlockTileEntity(x, y, z));
                    }
                    world.setBlockWithNotify(x, y, z, isOutline ? block.getBlockId() : 0);
                    world.setBlockMetadataWithNotify(x, y, z, isOutline ? block.getMetadata() : 0);
                }
            }
        }
        world.editingBlocks = false;

        return blocksFilled;
    }

    public static int fillOutline(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block) throws CommandSyntaxException {
        world.editingBlocks = true;
        int minX = Math.min(first.getX(source), second.getX(source));
        int minY = Math.min(first.getY(source, true), second.getY(source, true));
        int minZ = Math.min(first.getZ(source), second.getZ(source));
        int maxX = Math.max(first.getX(source), second.getX(source));
        int maxY = Math.max(first.getY(source, true), second.getY(source, true));
        int maxZ = Math.max(first.getZ(source), second.getZ(source));

        int blocksFilled = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isOutline = x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
                    if (isOutline && (block.getBlockId() != world.getBlockId(x, y, z) || block.getMetadata() != world.getBlockMetadata(x, y, z) || !CommanderHelper.blockEntitiesAreEqual(block.getTag(), CommanderHelper.tagFrom(world.getBlockTileEntity(x, y, z))))) ++blocksFilled;
                    if (isOutline) {
                        world.setBlockWithNotify(x, y, z, block.getBlockId());
                        world.setBlockMetadataWithNotify(x, y, z, block.getMetadata());
                        CommanderHelper.setTileEntity(world, x, y, z, block.getTag());
                    }
                }
            }
        }
        world.editingBlocks = false;

        return blocksFilled;
    }

    public static int fillKeep(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block) throws CommandSyntaxException {
        return fillReplace(source, world, first, second, block, new BlockInput(null, 0, new CompoundTag()));
    }

    public static int fillDestroy(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block) throws CommandSyntaxException {
        return fillReplace(source, world, first, second, block, null, true);
    }
}