package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class DoubleCoordinates {
    private final DoubleCoordinate x;
    private final DoubleCoordinate y;
    private final DoubleCoordinate z;

    public DoubleCoordinates(DoubleCoordinate x, DoubleCoordinate y, DoubleCoordinate z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX(double sourceX) throws CommandSyntaxException {
        return this.x.get(sourceX);
    }

    public double getY(double sourceY) throws CommandSyntaxException {
        return this.y.get(sourceY);
    }

    public double getZ(double sourceZ) throws CommandSyntaxException {
        return this.z.get(sourceZ);
    }

    public boolean hasRelativeCoordinates() {
        return this.x.isRelative() || this.y.isRelative() || this.z.isRelative();
    }
}
