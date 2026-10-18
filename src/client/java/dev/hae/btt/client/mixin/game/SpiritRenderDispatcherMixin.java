package dev.hae.btt.client.mixin.game;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import dev.hae.btt.client.spirit.SpiritCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin(EntityRenderDispatcher.class)
public class SpiritRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void btt$spiritHideCamera(E entity, Frustum frustum, double x, double y, double z,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof SpiritCamera) {
            cir.setReturnValue(false);
        }
    }
}
