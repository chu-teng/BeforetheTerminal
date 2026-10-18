package dev.hae.btt.mixin.game;

import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.record.GameRecordManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;








@Mixin(PlayerEntity.class)
public abstract class ItemDropRecordMixin {

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void bttRecordDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership,
                               CallbackInfoReturnable<ItemEntity> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity sp)) return;
        if (!SeatAssignment.isBttMode(sp.getWorld())) return;
        GameRecordManager.recordItemDrop(sp, stack);
    }
}
