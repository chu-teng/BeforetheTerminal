package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.hae.btt.registry.BttItems;










@Mixin(ItemEntity.class)
public abstract class PickupGateMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void bttPickupGate(PlayerEntity player, CallbackInfo ci) {
        ItemStack stack = ((ItemEntity) (Object) this).getStack();
        if (stack.isEmpty()) return;
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRunning()) return;


        if ((stack.isOf(WatheItems.REVOLVER) || stack.isOf(net.minecraft.item.Items.BOW))
                && btt$hasWeaponOfTheThree(player)) {
            ci.cancel();
            return;
        }

        if (!stack.isOf(WatheItems.REVOLVER) && !stack.isOf(net.minecraft.item.Items.BOW)
                && !stack.isOf(dev.hae.btt.registry.BttItems.MASTER_KEY)) return;

        if (!Roles.isPassengerCampFor(gwc, player) && !dev.hae.btt.role.Decoy.passengerFacade(player)) {

            if ((stack.isOf(WatheItems.REVOLVER) || stack.isOf(net.minecraft.item.Items.BOW))
                    && (dev.hae.btt.role.Colonel.pickupPrivilege(player)
                    || dev.hae.btt.trait.Traits.has(player, dev.hae.btt.trait.Traits.RECKLESS))) {
                return;
            }
            ci.cancel();
            return;
        }


    }


    private static boolean btt$hasWeaponOfTheThree(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.isOf(WatheItems.KNIFE) && !dev.hae.btt.role.Colonel.hypocrisy(player) || s.isOf(WatheItems.REVOLVER)
                    || s.isOf(net.minecraft.item.Items.BOW)) {
                return true;
            }
        }
        return false;
    }
}
