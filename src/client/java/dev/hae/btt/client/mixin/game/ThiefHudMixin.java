package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.client.BttClient;
import dev.hae.btt.component.ScavengerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class ThiefHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (dev.hae.btt.encounter.Memory.isBlind(net.minecraft.client.MinecraftClient.getInstance().player)) return;
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
                || !dev.hae.btt.game.SeatAssignment.isBttMode(client.world)) return;
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        AbilityState abilityPlayerComponent = (AbilityState) AbilityState.KEY.get(MinecraftClient.getInstance().player);
        if (gameWorldComponent.isRunning() && gameWorldComponent.isRole(MinecraftClient.getInstance().player, dev.hae.btt.role.Roles.THIEF)) {
            ScavengerState thiefState = ScavengerState.KEY.get(MinecraftClient.getInstance().player);
            int drawY = context.getScaledWindowHeight();

            Text line = dev.hae.btt.client.ui.SkillText.prompt(
                    Text.translatable("btt.tip.thief", BttClient.abilityBind.getBoundKeyLocalizedText()),
                    abilityPlayerComponent.cooldown,
                    Text.translatable("btt.hud.thief.progress", thiefState.bodiesEaten, thiefState.bodiesRequired));

            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, dev.hae.btt.role.Roles.THIEF.color());
        }
    }
}
