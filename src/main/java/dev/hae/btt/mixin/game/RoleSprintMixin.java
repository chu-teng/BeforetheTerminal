package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.role.SprintContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Role.class)
public abstract class RoleSprintMixin {
    @ModifyReturnValue(method = "getMaxSprintTime", at = @At("RETURN"), remap = false)
    private int bttCurrentSprintLimit(int original) { return SprintContext.limit((Role) (Object) this, original); }
}
