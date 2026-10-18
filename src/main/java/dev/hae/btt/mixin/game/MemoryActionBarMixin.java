package dev.hae.btt.mixin.game;

import dev.hae.btt.encounter.Memory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MemoryActionBarMixin {
    @Inject(method = "sendMessageToClient", at = @At("HEAD"), cancellable = true)
    private void bttMemorySilence(Text message, boolean overlay, CallbackInfo ci) {
        if (overlay && Memory.isBlind((ServerPlayerEntity) (Object) this) && !Memory.isGuessFeedback(message)) ci.cancel();
    }
}
