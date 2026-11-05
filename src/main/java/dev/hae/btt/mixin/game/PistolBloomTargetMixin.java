package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.item.PistolItem;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.entity.BloomEntity;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistolItem.class)
public abstract class PistolBloomTargetMixin {
    @Inject(method = "getGunTarget", at = @At("HEAD"), cancellable = true)
    private static void bttBloomTarget(PlayerEntity user, CallbackInfoReturnable<HitResult> cir) {
        if (SeatAssignment.isBttMode(user.getWorld()))
            cir.setReturnValue(ProjectileUtil.getCollision(user, entity -> entity instanceof BloomEntity && entity.isAlive()
                    || entity instanceof PlayerEntity player && GameFunctions.isPlayerAliveAndSurvival(player), 15));
    }
}
