package dev.hae.btt.mixin.game;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class ConversionItemsMixin {
    @Inject(method = "giveItemStack", at = @At("HEAD"), cancellable = true)
    private void bttNoConversionItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (dev.hae.btt.role.RoleTransitions.isConverting((PlayerEntity) (Object) this)) cir.setReturnValue(true);
    }
}
