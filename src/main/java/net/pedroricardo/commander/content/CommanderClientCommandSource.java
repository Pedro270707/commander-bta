package net.pedroricardo.commander.content;

import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.command.ClientCommandHandler;
import net.minecraft.core.net.command.ClientPlayerCommandSender;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.Commander;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommanderClientCommandSource implements CommanderCommandSource {
    private final Minecraft mc;

    public CommanderClientCommandSource(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public Collection<String> getPlayerNames() {
        List<String> list = new ArrayList<>();
        for (EntityPlayer player : this.mc.theWorld.players) {
            list.add(player.username);
        }
        return list;
    }

    @Override
    public Collection<String> getEntitySuggestions() {
        List<String> suggestions = new ArrayList<>(this.getPlayerNames());
        if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.entity != null) {
            suggestions.add(Commander.ENTITY_PREFIX + this.mc.objectMouseOver.entity.hashCode());
        }
        return suggestions;
    }

    @Override
    public String getType() {
        return "client";
    }

    @Override
    public EntityPlayer getSender() {
        return this.mc.thePlayer;
    }

    @Override
    public boolean hasAdmin() {
        return true;
    }

    @Override
    public @Nullable Vec3d getCoordinates() {
        return this.getSender().getPosition(1.0f);
    }

    @Override
    public @Nullable Vec3d getBlockCoordinates() {
        if (this.mc.objectMouseOver != null) {
            return Vec3d.createVector(this.mc.objectMouseOver.x, this.mc.objectMouseOver.y, this.mc.objectMouseOver.z);
        }
        Vec3d playerCoordinates = this.getCoordinates();
        return playerCoordinates == null ? null : Vec3d.createVector(playerCoordinates.xCoord, playerCoordinates.yCoord - 1.6, playerCoordinates.zCoord);
    }

    @Override
    public void sendMessage(String message) {
        this.mc.ingameGUI.addChatMessage(message);
    }

    @Override
    public World getWorld() {
        return this.getSender().world;
    }

    @Override
    public @Deprecated CommandHandler getCommandHandler() {
        return this.mc.commandHandler;
    }

    @Override
    public @Deprecated CommandSender getCommandSender() {
        return this.mc.thePlayer.sender;
    }
}
