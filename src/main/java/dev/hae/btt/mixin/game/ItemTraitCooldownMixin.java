package dev.hae.btt.mixin.game;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.entity.player.ItemCooldownManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(ItemCooldownManager.class)
public abstract class ItemTraitCooldownMixin {
    @ModifyVariable(method = "set", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int btt$itemCooldown(int ticks) {
        return (Object) this instanceof ServerCooldownOwnerAccessor owner
                ? TraitRules.itemCooldown(owner.btt$owner(), ticks) : ticks;
    }
}
