package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;















@Mixin(RoleNameRenderer.class)
public abstract class CohortLabelMixin {






    @Shadow private static PlayerEntity target;

    @Shadow private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void bttCohortLabel(TextRenderer renderer, ClientPlayerEntity player, DrawContext context,
                                       RenderTickCounter tickCounter, CallbackInfo ci) {
        if (dev.hae.btt.encounter.Memory.isBlind(player)) return;
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRunning()) return;
        if (target == null || target == player || nametagAlpha <= 0.05f) return;
        String key = dev.hae.btt.component.PlayerState.KEY.get(target).observedCohort;
        if (key.isEmpty()) return;

        Text line = Text.translatable(key).withColor(0xFF0000);
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F,
                context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
        context.getMatrices().scale(0.6F, 0.6F, 1.0F);
        context.drawTextWithShadow(renderer, line, -renderer.getWidth(line) / 2, 64, 0xFFFFFF);
        context.getMatrices().pop();
    }
}
