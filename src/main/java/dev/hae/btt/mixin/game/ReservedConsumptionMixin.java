package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.hae.btt.trait.TraitRules;
import dev.hae.btt.trait.Traits;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin({LivingEntity.class, PlayerEntity.class})
public abstract class ReservedConsumptionMixin {
    @WrapOperation(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void btt$quietConsumption(World world, PlayerEntity except, double x, double y, double z,
                                     SoundEvent sound, SoundCategory category, float volume, float pitch, Operation<Void> original) {
        if ((Object) this instanceof PlayerEntity player && TraitRules.active(player)
                && Traits.has(player, Traits.RESERVED) && TraitRules.silentAction(sound.getId())) return;
        original.call(world, except, x, y, z, sound, category, volume, pitch);
    }
}
