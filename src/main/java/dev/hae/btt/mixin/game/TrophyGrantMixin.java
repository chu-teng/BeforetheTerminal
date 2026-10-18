package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;









@Mixin(GameFunctions.class)
public abstract class TrophyGrantMixin {

    @Inject(method = "finalizeGame", at = @At("TAIL"))
    private static void bttGrantTrophies(ServerWorld world, CallbackInfo ci) {
        dev.hae.btt.game.Trophies.grantAll(world);
    }
}