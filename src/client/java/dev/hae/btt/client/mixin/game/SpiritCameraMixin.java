package dev.hae.btt.client.mixin.game;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import dev.hae.btt.client.spirit.SpiritCamera;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;






@Mixin(Camera.class)
public class SpiritCameraMixin {

    @Shadow private Entity focusedEntity;
    @Shadow private float cameraY;
    @Shadow private float lastCameraY;

    @Inject(method = "update", at = @At("HEAD"))
    private void btt$spiritFixEyeHeight(BlockView area, Entity newFocusedEntity, boolean thirdPerson,
                                        boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (newFocusedEntity == null || this.focusedEntity == null || newFocusedEntity.equals(this.focusedEntity)) {
            return;
        }
        if (newFocusedEntity instanceof SpiritCamera || this.focusedEntity instanceof SpiritCamera) {
            this.lastCameraY = this.cameraY = newFocusedEntity.getStandingEyeHeight();
        }
    }

    @Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
    private void btt$spiritNoSubmersion(CallbackInfoReturnable<CameraSubmersionType> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(CameraSubmersionType.NONE);
        }
    }
}
