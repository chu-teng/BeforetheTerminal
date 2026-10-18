package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.block_entity.TrimmedBedBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;






@Mixin(dev.doctor4t.wathe.util.PoisonUtils.class)
public abstract class BedPoisonPermanentMixin {

    @WrapOperation(method = "bedPoison", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/block_entity/TrimmedBedBlockEntity;setHasScorpion(ZLjava/util/UUID;)V"))
    private static void bttKeepScorpion(TrimmedBedBlockEntity bed, boolean flag, UUID poisoner, Operation<Void> op) {
        op.call(bed, true, bed.getPoisoner());
    }
}
