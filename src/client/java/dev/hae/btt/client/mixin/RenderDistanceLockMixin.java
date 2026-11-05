package dev.hae.btt.client.mixin;

import dev.doctor4t.ratatouille.client.util.OptionLocker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(OptionLocker.class)
public abstract class RenderDistanceLockMixin {
    @Inject(method = "overrideOption", at = @At("HEAD"), cancellable = true, remap = false)
    private static void btt$keepRenderDistance(String option, Object value, CallbackInfo ci) {
        if ("renderDistance".equals(option)) ci.cancel();
    }
}
