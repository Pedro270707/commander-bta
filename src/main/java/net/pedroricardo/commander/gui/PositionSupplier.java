package net.pedroricardo.commander.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.text.ITextField;

@FunctionalInterface
public interface PositionSupplier<T extends Number> {
    T get(ITextField parent, Gui child, Minecraft mc, boolean followParameters);
}
