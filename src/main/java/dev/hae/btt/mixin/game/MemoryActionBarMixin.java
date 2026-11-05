package dev.hae.btt.mixin.game;

import dev.hae.btt.encounter.Memory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(ServerPlayerEntity.class)
public abstract class MemoryActionBarMixin {
    @WrapMethod(method = "sendMessageToClient")
    private void bttMemoryFeedback(Text message, boolean overlay, Operation<Void> original) {
        if (overlay && Memory.isBlind((ServerPlayerEntity)(Object)this)) {
            message = Memory.feedback(message);
            if (message == null) return;
        }
        original.call(message, overlay);
    }
}
