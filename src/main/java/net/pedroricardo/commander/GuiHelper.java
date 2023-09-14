package net.pedroricardo.commander;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

public class GuiHelper {
    public static void drawDottedRect(Gui gui, int minX, int minY, int maxX, int maxY, int argb, int dotSize) {
        for (int i = 0; i < (maxX - minX) / dotSize; i++) {
            for (int j = 0; j < (maxY - minY) / dotSize; j++) {
                if ((i + j) % 2 == 0) {
                    gui.drawRect(minX + i * dotSize, minY + j * dotSize, minX + i * dotSize + dotSize, minY + j * dotSize + dotSize, argb);
                }
            }
        }
    }

    public static int getScaledMouseX(Minecraft mc) {
        return Mouse.getX() / mc.resolution.scale;
    }

    public static int getScaledMouseY(Minecraft mc) {
        return mc.resolution.scaledHeight - (Mouse.getY() / mc.resolution.scale);
    }
}
