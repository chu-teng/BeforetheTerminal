package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.hae.btt.draft.DraftManager;
import dev.hae.btt.game.BeforeTheTerminalGameMode;









@Mixin(GameFunctions.class)
public abstract class DraftStartGateMixin {

    @Inject(method = "startGame(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/api/GameMode;Ldev/doctor4t/wathe/api/MapEffect;I)V",
            at = @At("HEAD"), cancellable = true)
    private static void bttDraftGate(ServerWorld world, GameMode gameMode, MapEffect mapEffect, int time,
                                     CallbackInfo ci) {
        if (!(gameMode instanceof BeforeTheTerminalGameMode btt)) {
            return;
        }
        if (DraftManager.belongsToAnotherWorld(world)) { ci.cancel(); return; }
        var status = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world).getGameStatus();
        if (status != dev.doctor4t.wathe.cca.GameWorldComponent.GameStatus.INACTIVE) {
            ci.cancel();
            return;
        }
        if (!dev.hae.btt.game.ServerSettings.prepare(world, btt)) {
            ci.cancel();
            return;
        }
        if (btt.mode != BeforeTheTerminalGameMode.Mode.DRAFT) return;
        if (DraftManager.belongsToAnotherWorld(world)) {
            ci.cancel();
            return;
        }
        if (DraftManager.isResultReady()) return;
        if (DraftManager.isActive()) {
            ci.cancel();
            return;
        }
        DraftManager.rememberStart(world, btt, mapEffect, time);
        if (DraftManager.start(world)) {
            ci.cancel();
        }

    }

    @Inject(method = "startGame(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/api/GameMode;Ldev/doctor4t/wathe/api/MapEffect;I)V", at = @At("RETURN"))
    private static void bttClearFailedStart(ServerWorld world, GameMode mode, MapEffect effect, int time, CallbackInfo ci) {
        if (mode instanceof BeforeTheTerminalGameMode
                && dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world).getGameStatus()
                == dev.doctor4t.wathe.cca.GameWorldComponent.GameStatus.INACTIVE
                && !DraftManager.hasSession(world)) dev.hae.btt.game.ServerSettings.cancel(world);
    }
}
