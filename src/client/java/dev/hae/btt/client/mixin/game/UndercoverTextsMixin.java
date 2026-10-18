package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.BeforeTheTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RoleNameRenderer.class)
public class UndercoverTextsMixin {

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z", ordinal = 0))
    private static boolean renderCustomCohorts(GameWorldComponent instance, PlayerEntity player, Operation<Boolean> original) {



        if (dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())
                && instance.isRole(player, dev.hae.btt.role.Roles.UNDERCOVER)) {
            return true;
        }
        return original.call(instance, player);
    }
}
