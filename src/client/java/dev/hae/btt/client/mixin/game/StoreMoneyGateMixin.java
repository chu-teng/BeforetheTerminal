package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.StoreRenderer;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;











@Mixin(StoreRenderer.class)
public abstract class StoreMoneyGateMixin {

    @Inject(method = "renderHud", at = @At("HEAD"), cancellable = true)
    private static void btt$moneyOnlyForShopRoles(TextRenderer renderer, ClientPlayerEntity player,
                                                 DrawContext context, float delta, CallbackInfo ci) {
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        if (Roles.canUseKillerShop(GameWorldComponent.KEY.get(player.getWorld()).getRole(player))) return;
        ci.cancel();
    }
}
