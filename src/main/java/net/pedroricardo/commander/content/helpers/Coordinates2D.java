package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3d;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import org.jetbrains.annotations.Nullable;

public class Coordinates2D {
    private final IntegerCoordinate x;
    private final IntegerCoordinate z;

    public Coordinates2D(IntegerCoordinate x, IntegerCoordinate z) {
        this.x = x;
        this.z = z;
    }

    public Coordinates2D(int x, int z) {
        this(new IntegerCoordinate(false, x), new IntegerCoordinate(false, z));
    }

    public int getX(@Nullable Integer sourceX) throws CommandSyntaxException {
        return this.x.get(sourceX == null ? null : MathHelper.floor_double((double)sourceX / 16.0));
    }

    public int getZ(@Nullable Integer sourceZ) throws CommandSyntaxException {
        return this.z.get(sourceZ == null ? null : MathHelper.floor_double((double)sourceZ / 16.0));
    }

    public int getX(@Nullable Double sourceX) throws CommandSyntaxException {
        return this.x.get(sourceX == null ? null : MathHelper.floor_double(sourceX / 16.0));
    }

    public int getZ(@Nullable Double sourceZ) throws CommandSyntaxException {
        return this.z.get(sourceZ == null ? null : MathHelper.floor_double(sourceZ / 16.0));
    }

    public int getX(CommanderCommandSource source) throws CommandSyntaxException {
        Vec3d sourceCoordinates = source.getCoordinates(true);
        if (sourceCoordinates == null) {
            if (!this.x.isRelative()) {
                return this.x.get(0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.x.get(MathHelper.floor_double(sourceCoordinates.xCoord / 16.0));
    }

    public int getZ(CommanderCommandSource source) throws CommandSyntaxException {
        Vec3d sourceCoordinates = source.getCoordinates(true);
        if (sourceCoordinates == null) {
            if (!this.z.isRelative()) {
                return this.z.get(0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.z.get(MathHelper.floor_double(sourceCoordinates.zCoord / 16.0));
    }

    public boolean hasRelativeCoordinate() {
        return this.x.isRelative() || this.z.isRelative();
    }
}
