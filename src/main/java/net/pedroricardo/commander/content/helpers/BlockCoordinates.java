package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import org.jetbrains.annotations.Nullable;

public class BlockCoordinates {
    private final BlockCoordinate x;
    private final BlockCoordinate y;
    private final BlockCoordinate z;

    public BlockCoordinates(BlockCoordinate x, BlockCoordinate y, BlockCoordinate z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX(@Nullable Integer sourceX) throws CommandSyntaxException {
        return this.x.get(sourceX);
    }

    public int getY(@Nullable Integer sourceY) throws CommandSyntaxException {
        return this.y.get(sourceY);
    }

    public int getZ(@Nullable Integer sourceZ) throws CommandSyntaxException {
        return this.z.get(sourceZ);
    }

    public int getX(@Nullable Double sourceX) throws CommandSyntaxException {
        if (sourceX == null) return this.x.get(null);
        return this.x.get((int) Math.floor(sourceX));
    }

    public int getY(@Nullable Double sourceY) throws CommandSyntaxException {
        if (sourceY == null) return this.y.get(null);
        return this.y.get((int) Math.floor(sourceY));
    }

    public int getZ(@Nullable Double sourceZ) throws CommandSyntaxException {
        if (sourceZ == null) return this.z.get(null);
        return this.z.get((int) Math.floor(sourceZ));
    }

    public int getX(CommanderCommandSource source) throws CommandSyntaxException {
        if (source.getCoordinates() == null) {
            if (!this.x.isRelative()) {
                return this.x.get(0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.x.get((int) Math.floor(source.getCoordinates().xCoord));
    }

    public int getY(CommanderCommandSource source, boolean lowerToFeet) throws CommandSyntaxException {
        if (source.getCoordinates() == null) {
            if (!this.y.isRelative()) {
                return this.y.get(0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.y.get((int) (Math.floor(source.getCoordinates().yCoord - (lowerToFeet ? 1.6 : 0))));
    }

    public int getZ(CommanderCommandSource source) throws CommandSyntaxException {
        if (source.getCoordinates() == null) {
            if (!this.z.isRelative()) {
                return this.z.get(0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.z.get((int) Math.floor(source.getCoordinates().zCoord));
    }

    public boolean hasRelativeCoordinate() {
        return this.x.isRelative() || this.y.isRelative() || this.z.isRelative();
    }
}
