package dev.hae.btt.mixin.game;

import dev.hae.btt.map.MapEnhancements;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;












@Mixin(ServerPlayerInteractionManager.class)
public abstract class InteractionBlacklistMixin {

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void btt$mapInteractionBlacklist(ServerPlayerEntity player, World world, ItemStack stack, Hand hand,
                                             BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) return;

        if (MapEnhancements.blocksInteraction(world, hitResult.getBlockPos())) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
