package dev.hae.btt.client.map;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.map.MapEnhancements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;













public final class MapFog {
    private MapFog() {}


    private static @Nullable MapEnhancements.Fog activeFog() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) return null;
        var train = WatheClient.trainComponent;
        if (train == null || !train.isFoggy()) return null;
        if (player.hasStatusEffect(StatusEffects.BLINDNESS)) return null;
        MapEnhancements.Fog fog = MapEnhancements.activeFor(mc.world).fog();
        return fog == null || fog.isEmpty() ? null : fog;
    }


    public static void applyDistances() {
        MapEnhancements.Fog fog = activeFog();
        if (fog == null || !fog.hasDistances()) return;
        float start = fog.start() != null ? fog.start() : 0f;
        float end = WatheClient.isTrainMoving()
                ? (fog.endMoving() != null ? fog.endMoving() : 130f)
                : (fog.endStationary() != null ? fog.endStationary() : 200f);
        RenderSystem.setShaderFogStart(start);
        RenderSystem.setShaderFogEnd(end);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);
    }


    public static @Nullable Vec3d nightColor() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || player.hasStatusEffect(StatusEffects.BLINDNESS)) return null;

        MapEnhancements.Fog fog = MapEnhancements.activeFor(mc.world).fog();
        if (fog == null || fog.nightColor() == null) return null;
        int rgb = fog.nightColor();
        return new Vec3d(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
    }
}
