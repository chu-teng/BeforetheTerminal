package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(InGameHud.class)
public abstract class KidnapHudMixin {
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    private void bttKidnapHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!SeatAssignment.isBttMode(client.world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.world);
        if (!gwc.isRunning()) return;
        TextRenderer tr = getTextRenderer();
        int right = context.getScaledWindowWidth();


        PlayerState mine = PlayerState.KEY.get(client.player);
        if (mine.isSwallowed()) {
            int secs = mine.digestTicks / 20;
            Text line = mine.digestible
                    ? Text.translatable("btt.hud.kidnapped.timer", secs)
                    : Text.translatable("btt.hud.kidnapped.stuck");
            if ("SMUGGLER".equals(mine.attachKind)) {
                line = Text.translatable("btt.hud.smuggler.held");
            }

            context.drawTextWithShadow(tr, line, (right - tr.getWidth(line)) / 2,
                    context.getScaledWindowHeight() / 2 + 20, 0xFFFFFF);
            return;
        }

    }
}
