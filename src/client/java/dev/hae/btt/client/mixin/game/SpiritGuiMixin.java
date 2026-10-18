package dev.hae.btt.client.mixin.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin(InGameHud.class)
public class SpiritGuiMixin {

    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void btt$spiritUseRealPlayer(CallbackInfoReturnable<PlayerEntity> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(MinecraftClient.getInstance().player);
        }
    }
}
