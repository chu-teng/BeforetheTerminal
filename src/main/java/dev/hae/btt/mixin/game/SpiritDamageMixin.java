package dev.hae.btt.mixin.game;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.mechanic.SpiritWalk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;








@Mixin(LivingEntity.class)
public class SpiritDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void btt$spiritReturnOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (amount <= 0) return;
        if (!((Object) this instanceof ServerPlayerEntity player)) return;
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        if (PlayerState.KEY.get(player).isProjecting()) {
            SpiritWalk.forceReturn(player, net.minecraft.text.Text.translatable("btt.action.muyuubyou.interrupted"));
        }
    }
}
