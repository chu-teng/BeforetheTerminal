package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.block.FoodPlatterBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.hae.btt.role.ColdWave;
import org.spongepowered.asm.mixin.Mixin;







@Mixin(FoodPlatterBlock.class)
public abstract class PlateTakeMixin {

    @WrapMethod(method = "onUse")
    private ActionResult bttPlateTake(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                      BlockHitResult hit, Operation<ActionResult> original) {
        boolean emptyHand = player.getMainHandStack().isEmpty();
        ActionResult result = original.call(state, world, pos, player, hit);
        if (world.isClient() || !(player instanceof ServerPlayerEntity server)) return result;
        if (emptyHand && !player.getMainHandStack().isEmpty()
                && world.getBlockEntity(pos) instanceof BeveragePlateBlockEntity plate
                && plate.getStoredItems().stream().anyMatch(stack -> stack.isOf(player.getMainHandStack().getItem())))
            ColdWave.contaminate(server, pos);
        return result;
    }

}
