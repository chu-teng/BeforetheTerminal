package dev.hae.btt.mixin.game;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(value = AbilityState.class, remap = false)
public abstract class SkillTraitCooldownMixin {
    @Shadow @Final private PlayerEntity player;
    @ModifyVariable(method = "setCooldown", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private int btt$skillCooldown(int ticks) {
        return TraitRules.skillCooldown(player, ticks);
    }
}
