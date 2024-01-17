package net.pedroricardo.commander.content.helpers;

import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import org.jetbrains.annotations.Nullable;

public class ClonedBlock {
    @Nullable
    private final Block block;
    private final int metadata;
    @Nullable
    private final TileEntity tileEntity;

    public ClonedBlock(@Nullable Block block, int metadata, @Nullable TileEntity tileEntity) {
        this.block = block;
        this.metadata = metadata;
        this.tileEntity = tileEntity;
    }

    public @Nullable Block getBlock() {
        return this.block;
    }

    public int getBlockId() {
        return this.block == null ? 0 : this.block.id;
    }

    public int getMetadata() {
        return this.metadata;
    }

    public TileEntity getTileEntity() {
        return this.tileEntity;
    }
}