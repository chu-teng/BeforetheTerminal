package dev.hae.btt.client.mixin.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import dev.hae.btt.client.spirit.SpiritCamera;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;






@Mixin(Entity.class)
public class SpiritEntityMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void btt$spiritRedirectLook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (SpiritCameraHandler.isActive() && (Object) this == MinecraftClient.getInstance().player) {
            SpiritCamera camera = SpiritCameraHandler.getSpiritCamera();
            if (camera != null) {
                camera.changeLookDirection(cursorDeltaX, cursorDeltaY);
            }
            ci.cancel();
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void btt$spiritNoPush(Entity entity, CallbackInfo ci) {
        if (!SpiritCameraHandler.isActive()) return;
        SpiritCamera camera = SpiritCameraHandler.getSpiritCamera();
        if (camera != null && (entity == camera || (Object) this == camera)) {
            ci.cancel();
        }
    }
}
