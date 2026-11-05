package dev.hae.btt.mixin.game;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class DreamWakeMixin {
    @Unique private boolean btt$wasSleeping;
    @Inject(method = "wakeUp(ZZ)V", at = @At("HEAD"))
    private void btt$beforeWake(boolean skipTimer, boolean updatePlayers, CallbackInfo ci) {
        btt$wasSleeping = ((PlayerEntity)(Object)this).isSleeping();
    }
    @Inject(method = "wakeUp(ZZ)V", at = @At("RETURN"))
    private void btt$afterWake(boolean skipTimer, boolean updatePlayers, CallbackInfo ci) {
        if (btt$wasSleeping && (Object)this instanceof ServerPlayerEntity player)
            dev.hae.btt.role.LucidDream.wokeUp(player);
        btt$wasSleeping = false;
    }
}
