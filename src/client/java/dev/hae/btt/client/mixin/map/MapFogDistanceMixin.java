package dev.hae.btt.client.mixin.map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.hae.btt.client.map.MapFog;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;








@Mixin(WorldRenderer.class)
public class MapFogDistanceMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V"))
    private void btt$mapFogDistances(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance,
                                     boolean thickFog, float tickDelta, Operation<Void> original) {
        original.call(camera, fogType, viewDistance, thickFog, tickDelta);
        MapFog.applyDistances();
    }
}
