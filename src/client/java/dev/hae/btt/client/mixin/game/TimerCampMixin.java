package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.TimeRenderer;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;






@Mixin(TimeRenderer.class)
public abstract class TimerCampMixin {

    @ModifyExpressionValue(method = "renderHud",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/api/Role;canSeeTime()Z"),
            require = 0, expect = 1)
    private static boolean bttCampSeeTime(boolean original, TextRenderer renderer, ClientPlayerEntity player,
                                          DrawContext context, float delta) {
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return original;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (gwc == null || !gwc.isRunning()) return original;
        return Roles.canSeeTimeFor(gwc, player);
    }
}