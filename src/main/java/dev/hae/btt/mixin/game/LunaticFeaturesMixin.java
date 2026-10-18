package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;








@Mixin(GameWorldComponent.class)
public abstract class LunaticFeaturesMixin {

    @Inject(method = "canUseKillerFeatures", at = @At("HEAD"), cancellable = true)
    private void bttLunaticKillerFeatures(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        GameWorldComponent self = (GameWorldComponent) (Object) this;


        if (Roles.hasCurrentOrPriorRole(self, player, Roles.MARIONETTE)) { cir.setReturnValue(false); return; }
        if (Roles.hasCurrentOrPriorRole(self, player, Roles.LUNATIC)) cir.setReturnValue(true);
    }
    @Inject(method = "isInnocent", at = @At("HEAD"), cancellable = true)
    private void bttColonelPickupCheck(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (dev.hae.btt.role.Colonel.inPickup() && dev.hae.btt.role.Colonel.pickupPrivilege(player)) cir.setReturnValue(true);
    }

}
