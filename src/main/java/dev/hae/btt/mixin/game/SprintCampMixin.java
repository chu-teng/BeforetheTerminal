package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.hae.btt.role.SprintContext;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(PlayerEntity.class)
public abstract class SprintCampMixin {
    @WrapMethod(method = "tickMovement")
    private void bttCampSprintLimit(Operation<Void> original) {
        SprintContext.run((PlayerEntity) (Object) this, original::call);
    }
}
