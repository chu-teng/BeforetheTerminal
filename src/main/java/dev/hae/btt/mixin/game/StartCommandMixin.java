package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.command.StartCommand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.draft.DraftManager;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.map.MapEnhancements;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;




@Mixin(StartCommand.class)
public abstract class StartCommandMixin {


    @WrapOperation(method = {"execute", "lambda$execute$3"}, at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;startGame(Lnet/minecraft/server/world/ServerWorld;Ldev/doctor4t/wathe/api/GameMode;Ldev/doctor4t/wathe/api/MapEffect;I)V"))
    private static void bttExplicitStart(ServerWorld world, GameMode mode, MapEffect effect, int time,
                                         Operation<Void> original) {
        boolean marked = mode instanceof BeforeTheTerminalGameMode && !DraftManager.hasSession(world);
        if (marked) MapEnhancements.markExplicitEffect(world);
        boolean completed = false;
        try {
            original.call(world, mode, effect, time);
            completed = true;
        } finally {
            if (marked && (!completed || (GameWorldComponent.KEY.get(world).getGameStatus()
                    != GameWorldComponent.GameStatus.STARTING && !DraftManager.isActiveFor(world)))) {
                MapEnhancements.consumeExplicitEffect(world);
                DraftManager.reset(world);
            }
        }
    }
}
