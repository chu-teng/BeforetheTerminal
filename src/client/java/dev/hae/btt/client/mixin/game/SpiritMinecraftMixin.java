package dev.hae.btt.client.mixin.game;

import net.minecraft.client.MinecraftClient;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin(MinecraftClient.class)
public class SpiritMinecraftMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockAttack(CallbackInfoReturnable<Boolean> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockItemPick(CallbackInfo ci) {
        if (SpiritCameraHandler.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (SpiritCameraHandler.isActive()) {
            ci.cancel();
        }
    }
}
