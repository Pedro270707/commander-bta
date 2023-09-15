package net.pedroricardo.commander.mixin;

import net.minecraft.core.achievement.stat.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StatBase.class, remap = false)
public interface StatNameAccessor {
    @Accessor("statName")
    String statName();
}
