package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;







@Mixin(MoodRenderer.class)
public abstract class MoodGateMixin {

    @WrapOperation(method = "renderHud",
        at = @At(value = "INVOKE",
                 target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;getGameMode()Ldev/doctor4t/wathe/api/GameMode;"))
    private static GameMode bttMoodGate(GameWorldComponent instance, Operation<GameMode> original) {
        GameMode mode = original.call(instance);
        if (mode instanceof BeforeTheTerminalGameMode) return WatheGameModes.MURDER;
        return mode;
    }
}
