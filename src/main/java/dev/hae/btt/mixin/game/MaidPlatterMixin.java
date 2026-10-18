package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.block.FoodPlatterBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;







@Mixin(FoodPlatterBlock.class)
public abstract class MaidPlatterMixin {

    @Inject(method = "onUse", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/block_entity/BeveragePlateBlockEntity;getStoredItems()Ljava/util/List;", ordinal = 0),
            cancellable = true)
    private void bttMaidPickup(BlockState state, World world, BlockPos pos, PlayerEntity player,
                               BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient) return;
        if (!SeatAssignment.isBttMode(world)) return;
        if (!(world.getBlockEntity(pos) instanceof BeveragePlateBlockEntity blockEntity)) return;
        if (!dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world).isRole(player, Roles.MAID)) return;
        if (!player.getStackInHand(Hand.MAIN_HAND).isEmpty()) return;

        List<ItemStack> platter = blockEntity.getStoredItems();
        if (platter.isEmpty()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (heldFromPlatter(player, platter) >= 2) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }
        ItemStack randomItem = platter.get(world.random.nextInt(platter.size())).copy();
        randomItem.setCount(1);
        randomItem.set(DataComponentTypes.MAX_STACK_SIZE, 1);
        String poisoner = blockEntity.getPoisoner();
        if (poisoner != null) {
            randomItem.set(WatheDataComponentTypes.POISONER, poisoner);
            blockEntity.setPoisoner(null);
        }
        player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
        player.setStackInHand(Hand.MAIN_HAND, randomItem);

        cir.setReturnValue(ActionResult.SUCCESS);
    }


    private static int heldFromPlatter(PlayerEntity player, List<ItemStack> platter) {
        java.util.Set<net.minecraft.item.Item> types = new java.util.HashSet<>();
        for (ItemStack platterItem : platter) types.add(platterItem.getItem());
        int n = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack invItem = player.getInventory().getStack(i);
            if (!invItem.isEmpty() && types.contains(invItem.getItem())) n++;
        }
        return n;
    }
}
