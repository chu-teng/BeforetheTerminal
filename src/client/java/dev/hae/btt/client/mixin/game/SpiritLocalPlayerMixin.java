package dev.hae.btt.client.mixin.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;






@Mixin(ClientPlayerEntity.class)
public class SpiritLocalPlayerMixin {

    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void btt$spiritKeepCamera(CallbackInfoReturnable<Boolean> cir) {
        if (SpiritCameraHandler.isActive() && (Object) this == MinecraftClient.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
