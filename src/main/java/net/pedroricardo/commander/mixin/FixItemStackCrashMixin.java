package net.pedroricardo.commander.mixin;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.Tag;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = ItemStack.class, remap = false)
public class FixItemStackCrashMixin {
    @Inject(method = "canStackWith", at = @At(value = "INVOKE", target = "Lcom/mojang/nbt/CompoundTag;getTag(Ljava/lang/String;)Lcom/mojang/nbt/Tag;", ordinal = 0, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void commander$returnIfStackIsNull(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir, CompoundTag nbt1, CompoundTag nbt2, Map<String, Tag<?>> data1, Map<String, Tag<?>> data2, Iterator<?> var6, String key) {
        if (nbt2.getTag(key) == null) cir.setReturnValue(false);
    }
}
