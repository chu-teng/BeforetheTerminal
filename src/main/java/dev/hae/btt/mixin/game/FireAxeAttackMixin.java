package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.hae.btt.registry.BttItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;














@Mixin(PlayerEntity.class)
public abstract class FireAxeAttackMixin {

    @WrapMethod(method = "attack")
    private void bttFireAxeAttack(Entity target, Operation<Void> original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().isClient() || !self.getMainHandStack().isOf(BttItems.FIRE_AXE)) {
            original.call(target);
            return;
        }
        if (self.getItemCooldownManager().isCoolingDown(BttItems.FIRE_AXE)) {
            return;
        }
        if (!(target instanceof PlayerEntity victim)
                || !GameFunctions.isPlayerAliveAndSurvival(self)
                || !GameFunctions.isPlayerAliveAndSurvival(victim)) {
            original.call(target);
            return;
        }
        if (self.getAttackCooldownProgress(0.5F) < 1f) {
            original.call(target);
            return;
        }
        GameFunctions.killPlayer(victim, true, self, dev.hae.btt.combat.DeathReasons.AXE);
        self.getEntityWorld().playSound(self, victim.getX(), victim.getEyeY(), victim.getZ(),
                WatheSounds.ITEM_BAT_HIT, SoundCategory.PLAYERS, 3f, 1f);
    }
}
