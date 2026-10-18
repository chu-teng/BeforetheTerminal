package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.hae.btt.role.Colonel;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(value = ItemEntity.class, priority = 2000)
public abstract class ColonelPickupMixin {
    @WrapMethod(method = "onPlayerCollision")
    private void bttColonelPickup(PlayerEntity player, Operation<Void> original) {
        if (!Colonel.pickupPrivilege(player)) { original.call(player); return; }
        boolean previous = Colonel.beginPickup();
        try { original.call(player); } finally { Colonel.endPickup(previous); }
    }
}
