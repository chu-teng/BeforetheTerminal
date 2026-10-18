package dev.hae.btt.mixin.game;

import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.record.GameRecordManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;








@Mixin(ItemEntity.class)
public abstract class ItemPickupRecordMixin {

    @Unique
    private ItemStack btt$pickupCandidate = null;

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void bttCapturePickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!(player instanceof ServerPlayerEntity sp) || !SeatAssignment.isBttMode(sp.getWorld())) {
            btt$pickupCandidate = null;
            return;
        }
        btt$pickupCandidate = self.getStack().copy();
    }

    @Inject(method = "onPlayerCollision", at = @At("TAIL"))
    private void bttRecordPickup(PlayerEntity player, CallbackInfo ci) {
        ItemStack captured = btt$pickupCandidate;
        btt$pickupCandidate = null;
        if (captured == null || captured.isEmpty()) return;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        ItemEntity self = (ItemEntity) (Object) this;
        boolean taken = self.isRemoved() || self.getStack().getCount() < captured.getCount();
        if (taken) GameRecordManager.recordItemPickup(sp, captured);
    }
}
