package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.block_entity.DoorBlockEntity;
import dev.hae.btt.role.Locksmith;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;






@Mixin(DoorBlockEntity.class)
public abstract class DoorStateMixin {

    @Inject(method = "jam", at = @At("TAIL"))
    private void bttOnDoorJammed(CallbackInfo ci) {
        Locksmith.onDoorTampered((DoorBlockEntity) (Object) this);
    }

    @Inject(method = "blast", at = @At("TAIL"))
    private void bttOnDoorBlasted(CallbackInfo ci) {
        Locksmith.onDoorTampered((DoorBlockEntity) (Object) this);
    }

}
