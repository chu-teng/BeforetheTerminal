package dev.hae.btt.mixin.game;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.mechanic.DaggerKnockback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;










@Mixin(LivingEntity.class)
public abstract class DaggerKnockbackMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void bttDaggerKnockback(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) DaggerKnockback.tick(player);
    }
}
