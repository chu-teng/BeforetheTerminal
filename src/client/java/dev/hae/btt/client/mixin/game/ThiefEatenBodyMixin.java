package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.component.CorpseState;
import dev.hae.btt.component.ScavengerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerBodyEntityRenderer.class)
public abstract class ThiefEatenBodyMixin {

    @Inject(method = "renderBody", at = @At("TAIL"), cancellable = true)
    public void thiefSkeletonOnly(PlayerBodyEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, float alpha, CallbackInfo ci) {
        CorpseState bodyDeathReasonComponent = CorpseState.KEY.get(livingEntity);
        if (bodyDeathReasonComponent.scavenged) {
            ci.cancel();
        }
    }
}
