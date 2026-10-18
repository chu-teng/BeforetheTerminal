package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.client.BttClient;
import dev.hae.btt.component.DebateState;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;






@Mixin(InGameHud.class)
public abstract class DebateHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void bttDebateHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.options.hudHidden) return;
        if (!SeatAssignment.isBttMode(client.world)) return;
        if (!GameWorldComponent.KEY.get(client.world).isRunning()) return;
        DebateState debate = DebateState.KEY.get(client.world);
        if (debate.phase == DebateState.PHASE_IDLE) return;
        int seconds = Math.max(0, debate.ticksLeft / 20);
        Text line = debate.phase == DebateState.PHASE_MEETING
                ? Text.translatable("btt.debate.hud.meeting", seconds)
                : Text.translatable("btt.debate.hud.voting", seconds);
        int width = client.getWindow().getScaledWidth();
        context.drawTextWithShadow(client.textRenderer, line,
                (width - client.textRenderer.getWidth(line)) / 2, 6, 0xFFFFFF);
    }
}
