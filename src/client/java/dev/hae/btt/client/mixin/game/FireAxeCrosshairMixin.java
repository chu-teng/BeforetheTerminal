package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.client.gui.CrosshairRenderer;
import dev.hae.btt.registry.BttItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;









@Mixin(CrosshairRenderer.class)
public class FireAxeCrosshairMixin {

    private static final int W = 10;
    private static final int H = 7;
    private static final Identifier ATTACK = Identifier.of("wathe", "hud/bat_attack");
    private static final Identifier PROGRESS = Identifier.of("wathe", "hud/bat_progress");
    private static final Identifier BACKGROUND = Identifier.of("wathe", "hud/bat_background");

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private static void btt$fireAxeStatus(MinecraftClient client, ClientPlayerEntity player,
                                          DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!client.options.getPerspective().isFirstPerson()) return;
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isOf(BttItems.FIRE_AXE)) return;

        ItemCooldownManager cooldowns = player.getItemCooldownManager();
        float tickDelta = tickCounter.getTickDelta(true);

        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f, 0);

        if (cooldowns.isCoolingDown(BttItems.FIRE_AXE)) {
            float progress = 1f - cooldowns.getCooldownProgress(BttItems.FIRE_AXE, tickDelta);
            context.drawGuiTexture(BACKGROUND, -5, 5, W, H);
            context.drawGuiTexture(PROGRESS, W, H, 0, 0, -5, 5, (int) (progress * W), H);
        } else if (player.getAttackCooldownProgress(tickDelta) >= 1f
                && client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit
                && hit.getEntity() instanceof net.minecraft.entity.player.PlayerEntity) {
            context.drawGuiTexture(ATTACK, -5, 5, W, H);
        } else {
            float charge = player.getAttackCooldownProgress(tickDelta);
            context.drawGuiTexture(BACKGROUND, -5, 5, W, H);
            context.drawGuiTexture(PROGRESS, W, H, 0, 0, -5, 5, (int) (charge * W), H);
        }

        context.getMatrices().pop();
    }
}
