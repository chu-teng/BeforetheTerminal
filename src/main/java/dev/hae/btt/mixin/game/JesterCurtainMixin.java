package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(PlayerPsychoComponent.class)
public abstract class JesterCurtainMixin {
    @Shadow @Final private PlayerEntity player;

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/PlayerPsychoComponent;stopPsycho()V"))
    private void btt$curtain(PlayerPsychoComponent psycho, Operation<Void> original) {
        original.call(psycho);
        if (SeatAssignment.isBttMode(player.getWorld())
                && GameWorldComponent.KEY.get(player.getWorld()).isRunning()
                && Roles.hasCurrentOrPriorRole(GameWorldComponent.KEY.get(player.getWorld()), player, Roles.JESTER)
                && GameFunctions.isPlayerAliveAndSurvival(player))
            GameFunctions.killPlayer(player, true, player, DeathReasons.JESTER_CURTAIN);
    }
}
