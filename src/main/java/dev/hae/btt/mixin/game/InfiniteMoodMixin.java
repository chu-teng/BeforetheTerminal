package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.hae.btt.trait.Traits;











@Mixin(PlayerMoodComponent.class)
public abstract class InfiniteMoodMixin {

    @Shadow
    private PlayerEntity player;

    @org.spongepowered.asm.mixin.injection.Inject(method = "setMood", at = @At("HEAD"), cancellable = true)
    private void btt$talkative(float value, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        var component = (PlayerMoodComponent) (Object) this;
        if (value < component.getMood() && dev.hae.btt.trait.TraitRules.active(player)
                && Traits.has(player, Traits.TALKATIVE) && dev.hae.btt.trait.TraitEffects.speaking(player)) ci.cancel();
    }


    @Redirect(method = "serverTick",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Ldev/doctor4t/wathe/cca/PlayerMoodComponent;setMood(F)V"))
    private void btt$redirectFirstSetMood(PlayerMoodComponent self, float value) {
        if (Traits.has(this.player, Traits.OPTIMIST)) {
            self.setMood(1f);
            return;
        }
        self.setMood(value);
    }
}
