package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.client.WatheClient;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.data.client.Models;
import net.minecraft.util.Identifier;
import dev.hae.btt.component.ConfigState;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.client.BttClient;
import dev.hae.btt.client.renderer.MorphUtil;
import dev.hae.btt.component.DisguiseState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class DisguiseRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public DisguiseRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Shadow public abstract Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity);

    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    void renderMorphlingSkin(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfoReturnable<Identifier> cir) {
        if (BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE == null) return;
        if (WatheClient.moodComponent != null) {
            if ((ConfigState.KEY.get(abstractClientPlayerEntity.getWorld())).insaneSeesMorphs && WatheClient.moodComponent.isLowerThanDepressed() && BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(abstractClientPlayerEntity.getUuid())) {
                cir.setReturnValue(WatheClient.PLAYER_ENTRIES_CACHE.get(BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(abstractClientPlayerEntity.getUuid())).getSkinTextures().texture());
                cir.cancel();
            }
        }
        if ((DisguiseState.KEY.get(abstractClientPlayerEntity)).getMorphTicks() > 0 ) {
            if (abstractClientPlayerEntity.getEntityWorld().getPlayerByUuid((DisguiseState.KEY.get(abstractClientPlayerEntity)).disguise) != null) {
                Identifier tex = getTexture((AbstractClientPlayerEntity) abstractClientPlayerEntity.getEntityWorld().getPlayerByUuid((DisguiseState.KEY.get(abstractClientPlayerEntity)).disguise));
                cir.setReturnValue(tex);

                cir.cancel();
            } else {
                Log.info(LogCategory.GENERAL, "Morphling disguise is null!!!");
            }
            if (DisguiseState.KEY.get(abstractClientPlayerEntity).disguise.equals(MinecraftClient.getInstance().player.getUuid())) {
                cir.setReturnValue(getTexture(MinecraftClient.getInstance().player));
                cir.cancel();
            }
        }
    }
    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    void applyModels(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE == null) return;
        if (WatheClient.moodComponent != null) {
            if ((ConfigState.KEY.get(abstractClientPlayerEntity.getWorld())).insaneSeesMorphs && WatheClient.moodComponent.isLowerThanDepressed() && BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(abstractClientPlayerEntity.getUuid())) {



                var shuffledEntry = WatheClient.PLAYER_ENTRIES_CACHE.get(
                        BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(abstractClientPlayerEntity.getUuid()));
                if (shuffledEntry != null
                        && shuffledEntry.getSkinTextures().model().equals(SkinTextures.Model.SLIM)) {
                    model = MorphUtil.SLIM;
                } else {
                    model = MorphUtil.CLASSIC;
                }
            }
        }
        if ((DisguiseState.KEY.get(abstractClientPlayerEntity)).getMorphTicks() > 0 ) {

            var disguisePlayer = abstractClientPlayerEntity.getEntityWorld().getPlayerByUuid((DisguiseState.KEY.get(abstractClientPlayerEntity)).disguise);
            if (disguisePlayer instanceof AbstractClientPlayerEntity abstractDisguise) {
                if (abstractDisguise.getSkinTextures().model().equals(SkinTextures.Model.SLIM)) {
                    model = MorphUtil.SLIM;
                } else {
                    model = MorphUtil.CLASSIC;
                }
            }
            if (DisguiseState.KEY.get(abstractClientPlayerEntity).disguise.equals(MinecraftClient.getInstance().player.getUuid())) {
                if (MinecraftClient.getInstance().player.getSkinTextures().model().equals(SkinTextures.Model.SLIM)) {
                    model = MorphUtil.SLIM;
                } else {
                    model = MorphUtil.CLASSIC;
                }
            }
        }
        if (abstractClientPlayerEntity.getSkinTextures().model().equals(SkinTextures.Model.SLIM)) {
            model = MorphUtil.SLIM;
        } else {
            model = MorphUtil.CLASSIC;
        }
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    void getModels(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        MorphUtil.CLASSIC = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false);
        MorphUtil.SLIM = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), true);
    }
    @WrapOperation(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;"))
    SkinTextures renderArm(AbstractClientPlayerEntity instance, Operation<SkinTextures> original) {
        if (BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE == null) return original.call(instance);
        if ((DisguiseState.KEY.get(instance)).getMorphTicks() > 0) {
            if (instance.getEntityWorld().getPlayerByUuid((DisguiseState.KEY.get(instance)).disguise) != null) {
                 return ((AbstractClientPlayerEntity) instance.getEntityWorld().getPlayerByUuid((DisguiseState.KEY.get(instance)).disguise)).getSkinTextures();
            } else {
                Log.info(LogCategory.GENERAL, "Morphling disguise is null!!!");
            }
        }
        if (WatheClient.moodComponent != null) {
            if ((ConfigState.KEY.get(instance.getWorld())).insaneSeesMorphs && WatheClient.moodComponent.isLowerThanDepressed() && BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(instance.getUuid())) {
                return WatheClient.PLAYER_ENTRIES_CACHE.get(BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(instance.getUuid())).getSkinTextures();
            }
        }
        return original.call(instance);
    }

}
