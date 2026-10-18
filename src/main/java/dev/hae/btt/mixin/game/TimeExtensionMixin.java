package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;















@Mixin(GameTimeComponent.class)
public abstract class TimeExtensionMixin {

    @Inject(method = "addTime", at = @At("HEAD"), cancellable = true)
    private void btt$uncappedAddTime(int time, CallbackInfo ci) {
        GameTimeComponent self = (GameTimeComponent) (Object) this;
        if (!(self.world instanceof ServerWorld serverWorld)) return;
        if (!SeatAssignment.isBttMode(serverWorld)) return;





        if (!dev.hae.btt.component.GameState.KEY.get(serverWorld).epilogueType.isEmpty() && time > 0) {
            ci.cancel();
            return;
        }
        self.setTime(self.getTime() + time);
        ci.cancel();
    }
}
