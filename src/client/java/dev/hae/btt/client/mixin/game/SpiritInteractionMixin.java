package dev.hae.btt.client.mixin.game;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin(ClientPlayerInteractionManager.class)
public class SpiritInteractionMixin {

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (SpiritCameraHandler.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult,
                                              CallbackInfoReturnable<ActionResult> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void btt$spiritBlockInteractEntity(PlayerEntity player, Entity entity, Hand hand,
                                               CallbackInfoReturnable<ActionResult> cir) {
        if (SpiritCameraHandler.isActive()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
