package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.block.SmallDoorBlock;
import dev.doctor4t.wathe.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;











@Mixin(SmallDoorBlock.class)
public abstract class BoltDoorMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void btt$boltDoor(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.getMainHandStack().isOf(WatheItems.LOCKPICK)) return;
        if (!SeatAssignment.isBttMode(world)) return;
        if (!GameWorldComponent.KEY.get(world).isRunning()) return;

        BlockPos lowerPos = state.get(SmallDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
        if (!(world.getBlockEntity(lowerPos) instanceof SmallDoorBlockEntity entity)) return;
        if (entity.isBlasted()) return;

        if (entity.isJammed()) {
            if (!world.isClient) {
                world.playSound(null, lowerPos.getX() + .5f, lowerPos.getY() + 1, lowerPos.getZ() + .5f,
                        WatheSounds.BLOCK_DOOR_LOCKED, SoundCategory.BLOCKS, 1f, 1f);
                player.sendMessage(Text.translatable("tip.door.jammed"), true);
            }
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }


        if (player instanceof net.minecraft.server.network.ServerPlayerEntity p) dev.hae.btt.advancement.BttAdvancements.fact(p, "bolt");
        entity.jam();
        SmallDoorBlockEntity neighbor = SmallDoorBlock.getNeighborDoorEntity(state, world, lowerPos);
        if (neighbor != null) neighbor.jam();

        if (!player.isCreative()) {
            player.getItemCooldownManager().set(WatheItems.LOCKPICK,
                    GameConstants.ITEM_COOLDOWNS.getOrDefault(WatheItems.LOCKPICK, GameConstants.getInTicks(2, 0)));
        }
        if (!world.isClient) {
            world.playSound(null, lowerPos.getX() + .5f, lowerPos.getY() + 1, lowerPos.getZ() + .5f,
                    WatheSounds.ITEM_LOCKPICK_DOOR, SoundCategory.BLOCKS, 1f, 1f);
        }
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
