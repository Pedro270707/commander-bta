package net.pedroricardo.commander.content.helpers;

import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.Block;

public class BlockInput {
    private final Block block;
    private final int metadata;
    private final CompoundTag tag;

    public BlockInput(Block block, int metadata, CompoundTag tag) {
        this.block = block;
        this.metadata = metadata;
        this.tag = tag;
    }

    public Block getBlock() {
        return this.block;
    }

    public int getMetadata() {
        return this.metadata;
    }

    public CompoundTag getTag() {
        return this.tag;
    }
}
