package dev.hae.btt.client.mixin.game;

import dev.hae.btt.registry.BttItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;







@Mixin(PlayerEntityRenderer.class)
public class FireAxePoseMixin {

    @Inject(method = "getArmPose", at = @At("TAIL"), cancellable = true)
    private static void btt$fireAxeArmPose(AbstractClientPlayerEntity player, Hand hand,
                                           CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        if (player.getStackInHand(hand).isOf(BttItems.FIRE_AXE)) {
            cir.setReturnValue(BipedEntityModel.ArmPose.CROSSBOW_CHARGE);
        }
    }
}
