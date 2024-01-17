package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BlockArgumentType;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.BlockInput;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class FillCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.fill.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("fill")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("first", IntegerCoordinatesArgumentType.intCoordinates())
                        .then(RequiredArgumentBuilder.argument("second", IntegerCoordinatesArgumentType.intCoordinates())
                                .then(RequiredArgumentBuilder.argument("block", BlockArgumentType.block())
                                        .executes(c -> {
                                            IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                            IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                            BlockInput block = c.getArgument("block", BlockInput.class);
                                            if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                            int blocksFilled = fillReplace((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block);
                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                            return blocksFilled;
                                        })
                                        .then(LiteralArgumentBuilder.literal("replace")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                                    int blocksFilled = fillReplace((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block);
                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                })
                                                .then(RequiredArgumentBuilder.argument("filter", BlockArgumentType.block())
                                                        .executes(c -> {
                                                            IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                            IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                            BlockInput block = c.getArgument("block", BlockInput.class);
                                                            BlockInput filter = c.getArgument("filter", BlockInput.class);
                                                            if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                                            int blocksFilled = fillReplace((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block, filter);
                                                            ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                            return blocksFilled;
                                                        })))
                                        .then(LiteralArgumentBuilder.literal("hollow")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                                    int blocksFilled = fillHollow((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block);
                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))
                                        .then(LiteralArgumentBuilder.literal("outline")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                                    int blocksFilled = fillOutline((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block);
                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))
                                        .then(LiteralArgumentBuilder.literal("keep")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                                    int blocksFilled = fillKeep((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block);
                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))
                                        .then(LiteralArgumentBuilder.literal("destroy")
                                                .executes(c -> {
                                                    IntegerCoordinates first = c.getArgument("first", IntegerCoordinates.class);
                                                    IntegerCoordinates second = c.getArgument("second", IntegerCoordinates.class);
                                                    BlockInput block = c.getArgument("block", BlockInput.class);
                                                    if (getVolume((CommanderCommandSource)c.getSource(), first, second) > 32768) throw FAILURE.create();
                                                    int blocksFilled = fillDestroy((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), first, second, block);
                                                    ((CommanderCommandSource)c.getSource()).sendTranslatableMessage(blocksFilled == 1 ? "commands.commander.fill.success_single" : "commands.commander.fill.success_multiple", blocksFilled);
                                                    return blocksFilled;
                                                }))))));
    }

    public static int getVolume(CommanderCommandSource source, IntegerCoordinates first, IntegerCoordinates second) throws CommandSyntaxException {
        return (int) (MathHelper.abs(first.getX(source) - second.getX(source)) * MathHelper.abs(first.getY(source, true) - second.getY(source, true)) * MathHelper.abs(first.getZ(source) - second.getZ(source)));
    }

    public static int fillReplace(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block) throws CommandSyntaxException {
        return fillReplace(source, world, first, second, block, null);
    }

    public static int fillReplace(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block, @Nullable BlockInput blockToReplace) throws CommandSyntaxException {
        return fillReplace(source, world, first, second, block, blockToReplace, false);
    }

    public static int fillReplace(CommanderCommandSource source, World world, IntegerCoordinates first, IntegerCoordinates second, BlockInput block, @Nullable BlockInput blockToReplace, boolean destroy) throws CommandSyntaxException {
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
                    if ((block.getBlockId() != world.getBlockId(x, y, z) || block.getMetadata() != world.getBlockMetadata(x, y, z)) && (blockToReplace == null || (world.getBlockId(x, y, z) == blockToReplace.getBlockId() && world.getBlockMetadata(x, y, z) == blockToReplace.getMetadata()))) {
                        ++blocksFilled;
                        if (destroy && world.getBlock(x, y, z) != null) world.getBlock(x, y, z).getBreakResult(world, EnumDropCause.WORLD, x, y, z, world.getBlockMetadata(x, y, z), world.getBlockTileEntity(x, y, z));
                    }
                    if (blockToReplace == null || (world.getBlockId(x, y, z) == blockToReplace.getBlockId() && world.getBlockMetadata(x, y, z) == blockToReplace.getMetadata())) world.setBlockAndMetadataWithNotify(x, y, z, block.getBlockId(), block.getMetadata());
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
                    if ((isOutline && (block.getBlockId() != world.getBlockId(x, y, z) || block.getMetadata() != world.getBlockMetadata(x, y, z))) || (!isOutline && world.getBlockId(x, y, z) != 0)) {
                        ++blocksFilled;
                        if (!isOutline && world.getBlock(x, y, z) != null) world.getBlock(x, y, z).getBreakResult(world, EnumDropCause.WORLD, x, y, z, world.getBlockMetadata(x, y, z), world.getBlockTileEntity(x, y, z));
                    }
                    world.setBlockAndMetadataWithNotify(x, y, z, isOutline ? block.getBlockId() : 0, isOutline ? block.getMetadata() : 0);
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
                    if (isOutline && (block.getBlockId() != world.getBlockId(x, y, z) || block.getMetadata() != world.getBlockMetadata(x, y, z))) ++blocksFilled;
                    if (isOutline) world.setBlockAndMetadataWithNotify(x, y, z, block.getBlockId(), block.getMetadata());
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