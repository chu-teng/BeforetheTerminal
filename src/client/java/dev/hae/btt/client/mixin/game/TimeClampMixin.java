package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.client.gui.TimeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;






@Mixin(TimeRenderer.class)
public abstract class TimeClampMixin {

    @ModifyExpressionValue(
            method = "renderHud",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameTimeComponent;getTime()I"))
    private static int btt$clampNonNegative(int original) {
        return Math.max(0, original);
    }
}
