package net.pedroricardo.commander.gui;

public enum AlignmentType {
    TOP_LEFT(true, true),
    TOP_RIGHT(false, true),
    BOTTOM_LEFT(true, false),
    BOTTOM_RIGHT(false, false);

    private final boolean left;
    private final boolean top;

    AlignmentType(boolean left, boolean top) {
        this.left = left;
        this.top = top;
    }

    public boolean isLeft() {
        return this.left;
    }

    public boolean isTop() {
        return this.top;
    }
}
