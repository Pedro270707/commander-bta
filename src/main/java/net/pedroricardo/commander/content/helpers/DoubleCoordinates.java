package net.pedroricardo.commander.content.helpers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

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

    public double getX(CommanderCommandSource source) throws CommandSyntaxException {
        if (source.getCoordinates() == null) {
            if (!this.x.isRelative()) {
                return this.x.get(0.0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.x.get(source.getCoordinates().xCoord);
    }

    public double getY(CommanderCommandSource source, boolean lowerToFeet) throws CommandSyntaxException {
        if (source.getCoordinates() == null) {
            if (!this.y.isRelative()) {
                return this.y.get(0.0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.y.get(source.getCoordinates().yCoord - (lowerToFeet ? 1.6 : 0));
    }

    public double getZ(CommanderCommandSource source) throws CommandSyntaxException {
        if (source.getCoordinates() == null) {
            if (!this.z.isRelative()) {
                return this.z.get(0.0);
            } else {
                throw CommanderExceptions.notInWorld().create();
            }
        }
        return this.z.get(source.getCoordinates().zCoord);
    }

    public boolean hasRelativeCoordinates() {
        return this.x.isRelative() || this.y.isRelative() || this.z.isRelative();
    }
}
