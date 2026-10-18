package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import dev.hae.btt.component.PlayerState;





@Mixin(LivingEntity.class)
public abstract class FakeDeathPushMixin {

    @WrapMethod(method = "isPushable")
    private boolean bttFakeDeathNotPushable(Operation<Boolean> original) {
        if (btt$fakeDead()) return false;
        return original.call();
    }

    @WrapMethod(method = "pushAway")
    private void bttFakeDeathNoPush(Entity entity, Operation<Void> original) {
        if (btt$fakeDead()) return;
        original.call(entity);
    }

    private boolean btt$fakeDead() {
        return (Object) this instanceof PlayerEntity player && PlayerState.KEY.get(player).isFakeDead();
    }
}
