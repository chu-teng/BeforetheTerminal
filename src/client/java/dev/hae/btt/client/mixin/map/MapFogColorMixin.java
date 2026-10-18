package dev.hae.btt.client.mixin.map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.client.map.MapFog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;







@Mixin(BackgroundRenderer.class)
public class MapFogColorMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
    private static Vec3d btt$mapFogColor(Vec3d pos, CubicSampler.RgbFetcher rgbFetcher, Operation<Vec3d> original) {
        Vec3d base = original.call(pos, rgbFetcher);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || !WatheClient.isTrainMoving() || mc.world.getTimeOfDay() != 18000) return base;
        Vec3d custom = MapFog.nightColor();
        return custom != null ? custom : base;
    }
}
