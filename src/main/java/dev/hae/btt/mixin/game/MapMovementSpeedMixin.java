package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.hae.btt.map.MapEnhancements;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;











@Mixin(value = PlayerEntity.class, priority = 1100)
public abstract class MapMovementSpeedMixin {

    @ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
    private float btt$mapMovementSpeed(float original) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        MapEnhancements e = MapEnhancements.activeFor(self.getWorld());
        if (e.isDefault()) return original;
        return self.isSprinting()
                ? 0.1f * e.sprintSpeedMultiplier()
                : 0.07f * e.walkSpeedMultiplier();
    }
}
