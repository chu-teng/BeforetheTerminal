package dev.hae.btt.client.mixin.game;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import dev.hae.btt.component.PlayerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;









@Mixin(PlayerEntityRenderer.class)
public abstract class FakeDeathPoseMixin {

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void bttFakeDeathPose(AbstractClientPlayerEntity player, MatrixStack matrices, float animationProgress,
                                  float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        if (!PlayerState.KEY.get(player).isFakeDead()) return;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F - bodyYaw));
        matrices.translate(1.0F, 0.0F, 0.0F);
        matrices.translate(0.0F, 0.15F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
        ci.cancel();
    }
}