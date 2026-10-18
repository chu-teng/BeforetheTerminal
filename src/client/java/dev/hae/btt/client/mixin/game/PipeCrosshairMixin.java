package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.client.gui.CrosshairRenderer;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.item.PipeItem;
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
public class PipeCrosshairMixin {

    private static final int W = 10;
    private static final int H = 7;
    private static final Identifier BTT_ATTACK = Identifier.of("wathe", "hud/bat_attack");
    private static final Identifier BTT_PROGRESS = Identifier.of("wathe", "hud/bat_progress");
    private static final Identifier BTT_BACKGROUND = Identifier.of("wathe", "hud/bat_background");

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private static void btt$pipeStatus(MinecraftClient client, ClientPlayerEntity player,
                                       DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!client.options.getPerspective().isFirstPerson()) return;
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isOf(BttItems.PIPE)) return;

        ItemCooldownManager cooldowns = player.getItemCooldownManager();
        float tickDelta = tickCounter.getTickDelta(true);
        PlayerState state = PlayerState.KEY.get(player);

        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f, 0);

        if (state.pipeTicks > 0) {

            float left = Math.min(1f, state.pipeTicks / (float) PipeItem.WINDOW_TICKS);
            context.drawGuiTexture(BTT_ATTACK, -5, 5, W, H);
            context.drawGuiTexture(BTT_BACKGROUND, -5, 5, W, H);
            context.drawGuiTexture(BTT_PROGRESS, W, H, 0, 0, -5, 5, (int) (left * W), H);
        } else if (cooldowns.isCoolingDown(BttItems.PIPE)) {

            float progress = 1f - cooldowns.getCooldownProgress(BttItems.PIPE, tickDelta);
            context.drawGuiTexture(BTT_BACKGROUND, -5, 5, W, H);
            context.drawGuiTexture(BTT_PROGRESS, W, H, 0, 0, -5, 5, (int) (progress * W), H);
        } else {

            float charge = player.getAttackCooldownProgress(tickDelta);
            context.drawGuiTexture(BTT_BACKGROUND, -5, 5, W, H);
            context.drawGuiTexture(BTT_PROGRESS, W, H, 0, 0, -5, 5, (int) (charge * W), H);
        }

        context.getMatrices().pop();
    }
}
