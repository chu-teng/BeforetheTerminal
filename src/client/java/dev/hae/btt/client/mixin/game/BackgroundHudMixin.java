package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.GameState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
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
public abstract class BackgroundHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void bttBackgroundHint(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.options.hudHidden) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.world);
        if (!(gwc.getGameMode() instanceof BeforeTheTerminalGameMode)) return;
        if (!gwc.isRunning()) return;
        String script = GameState.KEY.get(client.world).script;
        if (script != null && !script.isEmpty()) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.translatable("btt.script.announce", Text.translatable("btt.script." + script)),
                    4, client.getWindow().getScaledHeight() - 24, 0xFFFFFF);
        }
        String id = GameState.KEY.get(client.world).background;
        if (id == null || id.isEmpty()) return;
        Text line = Text.translatable("btt.background.hud", Text.translatable("btt.background." + id));
        context.drawTextWithShadow(client.textRenderer, line, 4,
                client.getWindow().getScaledHeight() - 12, 0xFFFFFF);
    }
}