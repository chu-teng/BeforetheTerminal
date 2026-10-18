package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class TraitEffectOwnershipMixin {
    @WrapMethod(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z")
    private boolean bttEffectOwner(StatusEffectInstance effect, Entity source, Operation<Boolean> original) {
        if ((Object) this instanceof ServerPlayerEntity player)
            dev.hae.btt.trait.TraitStatusEffects.beforeExternalApplication(player, effect);
        boolean applied = original.call(effect, source);
        if ((Object) this instanceof ServerPlayerEntity player)
            dev.hae.btt.trait.TraitStatusEffects.externalApplication(player, effect, applied);
        return applied;
    }
}
