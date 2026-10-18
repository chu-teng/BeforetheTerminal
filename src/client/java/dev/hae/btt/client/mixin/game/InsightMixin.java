package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.role.Roles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = WatheClient.class, priority = 1600)
public abstract class InsightMixin {


    private static final int INSIGHT_GREEN = 0x4EDD35;

    @ModifyReturnValue(method = "isKiller", at = @At("RETURN"))
    private static boolean btt$insightIsKiller(boolean original) {
        if (original) return true;
        return btt$insightViewer();
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void btt$insightGreen(Entity target, CallbackInfoReturnable<Integer> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && dev.hae.btt.role.Voodoo.isRemnant(client.player)) {
            cir.setReturnValue(-1); return;
        }
        if (client.player != null && dev.hae.btt.game.SeatAssignment.isBttMode(client.player.getWorld())
                && dev.doctor4t.wathe.game.GameFunctions.isPlayerSpectatingOrCreative(client.player)) {
            if (target instanceof PlayerEntity p) {
                var role = Roles.realRole(GameWorldComponent.KEY.get(client.player.getWorld()), p);
                cir.setReturnValue(WatheClient.isInstinctEnabled() && role != null
                        && dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(p) ? role.color() : -1);
            }
            if (target instanceof dev.doctor4t.wathe.entity.PlayerBodyEntity body) {
                var role = GameWorldComponent.KEY.get(client.player.getWorld()).getRole(body.getPlayerUuid());
                cir.setReturnValue(WatheClient.isInstinctEnabled() && role != null ? role.color() : -1);
            }
            return;
        }
        if (!btt$insightViewer()) return;
        if (!WatheClient.isInstinctEnabled()) return;
        if (!(target instanceof PlayerEntity p) || p.isSpectator()) return;
        cir.setReturnValue(INSIGHT_GREEN);
        cir.cancel();
    }


    private static boolean btt$insightViewer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return false;
        if (!dev.hae.btt.trait.Traits.has(client.player, dev.hae.btt.trait.Traits.INSIGHT)) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.world);
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(client.world) || !gwc.isRunning()) return false;
        return dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(client.player);
    }
}
