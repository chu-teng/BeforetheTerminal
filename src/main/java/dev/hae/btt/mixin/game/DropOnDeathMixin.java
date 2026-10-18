package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.hae.btt.registry.BttItems;







@Mixin(GameFunctions.class)
public abstract class DropOnDeathMixin {

    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void bttDropOnDeath(ItemStack stack, PlayerEntity victim, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty() || !SeatAssignment.isBttMode(victim.getWorld())) return;

        boolean gunOrKey = stack.isOf(WatheItems.REVOLVER) || stack.isOf(dev.hae.btt.registry.BttItems.MASTER_KEY);
        if (!gunOrKey) return;

        cir.setReturnValue(Roles.isPassengerCampFor(GameWorldComponent.KEY.get(victim.getWorld()), victim));
    }
}
