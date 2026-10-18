package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.cca.AutoStartComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.world.ServerWorld;
import dev.hae.btt.game.GameModes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;





@Mixin(AutoStartComponent.class)
public abstract class AutoStartMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;startGame(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/api/GameMode;Ldev/doctor4t/wathe/api/MapEffect;I)V"))
    private void bttAutoStart(ServerWorld world, GameMode gameMode, MapEffect mapEffect, int time, Operation<Void> original) {

        GameMode btt = GameModes.modeForStart(
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world));
        original.call(world, btt, mapEffect, GameConstants.getInTicks(btt.defaultStartTime, 0));
    }
}
