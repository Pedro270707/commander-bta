package net.pedroricardo.commander.mixin;

import net.minecraft.core.achievement.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Stat.class, remap = false)
public interface StatNameAccessor {
    @Accessor("statName")
    String statName();
}
