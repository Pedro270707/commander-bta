package net.pedroricardo.commander.content;

import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.command.ClientCommandHandler;
import net.minecraft.core.net.command.ClientPlayerCommandSender;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.minecraft.core.util.phys.Vec3d;
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
}
