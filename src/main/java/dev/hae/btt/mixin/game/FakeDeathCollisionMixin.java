package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import dev.hae.btt.component.PlayerState;






@Mixin(Entity.class)
public abstract class FakeDeathCollisionMixin {

    @WrapMethod(method = "collidesWith")
    private boolean bttFakeDeathNoCollide(Entity other, Operation<Boolean> original) {
        Entity self = (Entity) (Object) this;
        if (btt$fakeDead(self) || btt$fakeDead(other)) return false;
        return original.call(other);
    }

    private static boolean btt$fakeDead(Entity entity) {
        return entity instanceof PlayerEntity player && PlayerState.KEY.get(player).isFakeDead();
    }
}
