package dev.hae.btt.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.registry.BttItems;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;





public final class AggressionTint {

    private AggressionTint() {}


    private static final float R = 1.0F;
    private static final float G = 0.72F;
    private static final float B = 0.72F;

    public static void register() {
        WorldRenderEvents.START.register(ctx -> {
            if (active()) RenderSystem.setShaderColor(R, G, B, 1.0F);
        });
        WorldRenderEvents.END.register(ctx -> RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F));
    }


    private static boolean active() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return false;
        if (!SeatAssignment.isBttMode(client.world)) return false;
        var p = client.player;
        if (PlayerPsychoComponent.KEY.get(p).getPsychoTicks() > 0) return true;
        if (p.getMainHandStack().isOf(BttItems.FIRE_AXE)
                || p.getOffHandStack().isOf(BttItems.FIRE_AXE)) return true;
        return PlayerState.KEY.get(p).pipeTicks > 0;
    }
}
