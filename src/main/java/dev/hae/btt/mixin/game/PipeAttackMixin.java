package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.item.ItemUse;
import org.spongepowered.asm.mixin.Mixin;
import dev.hae.btt.registry.BttItems;














@Mixin(PlayerEntity.class)
public abstract class PipeAttackMixin {

    @WrapMethod(method = "attack")
    private void bttPipeAttack(Entity target, Operation<Void> original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().isClient() || !self.getMainHandStack().isOf(BttItems.PIPE)) {
            original.call(target);
            return;
        }
        if (!ItemUse.inBttRound(self.getWorld())
                || !(target instanceof ServerPlayerEntity victim)
                || !GameFunctions.isPlayerAliveAndSurvival(self)
                || !GameFunctions.isPlayerAliveAndSurvival(victim)) {
            original.call(target);
            return;
        }
        PlayerState pc = PlayerState.KEY.get(self);

        if (self.getItemCooldownManager().isCoolingDown(BttItems.PIPE)) {
            return;
        }
        boolean noCharge = pc.pipeTicks > 0;
        if (!noCharge && self.getAttackCooldownProgress(0.5F) < 1f) {
            original.call(target);
            return;
        }
        GameFunctions.killPlayer(victim, true, self, DeathReasons.PIPE);

        self.getWorld().playSound(null, victim.getX(), victim.getEyeY(), victim.getZ(),
                dev.hae.btt.registry.Sounds.PIPE_HIT, SoundCategory.PLAYERS, 3f, 1f);
    }
}
