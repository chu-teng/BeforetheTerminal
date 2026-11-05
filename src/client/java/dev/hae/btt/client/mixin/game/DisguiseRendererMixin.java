package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.hae.btt.client.renderer.MorphUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class DisguiseRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public DisguiseRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    void btt$renderDisguiseSkin(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(MorphUtil.skin(player).texture());
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    void applyModels(AbstractClientPlayerEntity player, float f, float g, MatrixStack matrices, VertexConsumerProvider vertices, int light, CallbackInfo ci) {
        model = MorphUtil.skin(player).model() == SkinTextures.Model.SLIM ? MorphUtil.SLIM : MorphUtil.CLASSIC;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void getModels(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        MorphUtil.CLASSIC = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false);
        MorphUtil.SLIM = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), true);
    }

    @WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
    SkinTextures renderArm(AbstractClientPlayerEntity player, Operation<SkinTextures> original) {
        return MorphUtil.skin(player);
    }
}
