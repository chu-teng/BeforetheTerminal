package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(dev.doctor4t.wathe.client.gui.RoleNameRenderer.class)
public abstract class TraitorCohortMixin {

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static boolean bttNoTraitorCohort(GameWorldComponent instance, PlayerEntity p, Operation<Boolean> original) {
        if (SeatAssignment.isBttMode(p.getWorld())) {
            return false;
        }
        return original.call(instance, p);
    }
}
