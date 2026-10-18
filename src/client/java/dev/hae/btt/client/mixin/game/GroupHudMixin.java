package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(RoleNameRenderer.class)
public abstract class GroupHudMixin {





    @Shadow private static PlayerEntity target;

    @Shadow private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void bttGroupInspect(TextRenderer renderer, ClientPlayerEntity player, DrawContext context,
                                       RenderTickCounter tickCounter, CallbackInfo ci) {
        if (dev.hae.btt.encounter.Memory.isBlind(player)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!(gwc.getGameMode() instanceof BeforeTheTerminalGameMode)) return;
        if (!gwc.isRunning()) return;
        boolean noble = gwc.isRole(player, Roles.NOBLE);
        boolean balloonist = gwc.isRole(player, Roles.BALLOONIST);
        if (!noble && !balloonist) return;

        if (target == null || target == player || nametagAlpha <= 0.05f) return;

        String group = PlayerState.KEY.get(target).observedGroup;
        String expected = noble ? "btt.hud.kins" : "btt.hud.balloon_rep";
        if (!expected.equals(group)) return;
        Text label = Text.translatable(group);
        int color = noble ? Roles.NOBLE.color() : Roles.BALLOONIST.color();

        Text line = Text.translatable("btt.hud.group_tag", label).withColor(color);
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
        context.getMatrices().scale(0.6F, 0.6F, 1.0F);
        context.drawTextWithShadow(renderer, line, -renderer.getWidth(line) / 2, 34, color);
        context.getMatrices().pop();
    }
}
