package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.hae.btt.trait.Traits;









@Mixin(Item.class)
public abstract class GluttonItemMixin {

    @ModifyReturnValue(method = "getMaxUseTime", at = @At("RETURN"))
    private static int bttGluttonFastUse(int original, ItemStack stack, LivingEntity user) {
        if (!(user instanceof PlayerEntity player)) return original;
        if (original <= 1) return original;
        if (!Traits.has(player, Traits.GLUTTON)) return original;
        return 1;
    }
}
