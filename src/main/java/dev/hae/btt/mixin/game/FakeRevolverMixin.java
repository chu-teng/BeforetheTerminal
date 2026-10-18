package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.item.PistolItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;












@Mixin(PistolItem.class)
public abstract class FakeRevolverMixin {

    @Inject(method = "getGunTarget", at = @At("HEAD"), cancellable = true)
    private static void bttFakeRevolver(PlayerEntity user, CallbackInfoReturnable<HitResult> cir) {
        if (user.getMainHandStack().isOf(dev.hae.btt.registry.BttItems.FAKE_REVOLVER)) {
            cir.setReturnValue(null);
        }
    }
}