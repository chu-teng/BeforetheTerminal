package dev.hae.btt.mixin.game;
import dev.hae.btt.trait.Traits;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayerEntity.class)
public abstract class ReservedPlayerSoundsMixin {
    @Inject(method = "playSoundToPlayer", at = @At("HEAD"), cancellable = true)
    private void btt$quietSelfSound(SoundEvent sound, SoundCategory category, float volume, float pitch, CallbackInfo ci) {
        var player = (ServerPlayerEntity) (Object) this;
        if (TraitRules.active(player) && Traits.has(player, Traits.RESERVED) && TraitRules.silentAction(sound.getId())) ci.cancel();
    }
}
