package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.client.BttClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class StowawayHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
                || !dev.hae.btt.game.SeatAssignment.isBttMode(client.world)
                || dev.hae.btt.encounter.Memory.isBlind(client.player)) return;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(client.world);
        if (!gameWorldComponent.isRunning()) return;
        AbilityState abilityPlayerComponent = AbilityState.KEY.get(client.player);
        if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, dev.hae.btt.role.Roles.STOWAWAY)) {
            int drawY = context.getScaledWindowHeight();

            Text line = Text.translatable("btt.tip.stowaway", BttClient.abilityBind.getBoundKeyLocalizedText());

            if (abilityPlayerComponent.cooldown > 0) {
                line = Text.translatable("tip.btt.cooldown", (abilityPlayerComponent.cooldown + 19)/20);
            }

            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, dev.hae.btt.role.Roles.STOWAWAY.color());
        }
    }
}
