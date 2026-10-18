package dev.hae.btt.mixin.game;
import dev.hae.btt.trait.Traits;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class ReservedSoundsMixin {
    @Inject(method = "playSound(Lnet/minecraft/sound/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void btt$quietActions(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player && TraitRules.active(player)
                && Traits.has(player, Traits.RESERVED) && TraitRules.silentAction(sound.getId())) ci.cancel();
    }
}
