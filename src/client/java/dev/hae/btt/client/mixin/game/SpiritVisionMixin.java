package dev.hae.btt.client.mixin.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
















@Mixin(GameRenderer.class)
public abstract class SpiritVisionMixin {

    @Shadow
    protected abstract void loadPostProcessor(Identifier id);

    @Unique
    private static final Identifier BTT_SPIRIT_GRAYSCALE = Identifier.ofVanilla("shaders/post/spirit_grayscale.json");


    @Unique
    private static Identifier btt$loadedPost = null;

    @Unique
    private boolean btt$spiritGrayscale = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void btt$spiritGrayscale(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {

        Identifier want = null;
        if (SpiritCameraHandler.isActive() || btt$confusedMood()) {
            want = BTT_SPIRIT_GRAYSCALE;
        }
        if (want == null) {
            if (btt$spiritGrayscale) {
                MinecraftClient.getInstance().gameRenderer.disablePostProcessor();
                btt$spiritGrayscale = false;
                btt$loadedPost = null;
                btt$restoreRenderState();
            }
        } else if (!btt$spiritGrayscale || !want.equals(btt$loadedPost)) {
            loadPostProcessor(want);
            btt$spiritGrayscale = true;
            btt$loadedPost = want;

            btt$restoreRenderState();
        }
    }






    @Unique
    private static void btt$restoreRenderState() {
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    }


    @Unique
    private static boolean btt$confusedMood() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return false;
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(client.world)) return false;
        if (!dev.hae.btt.component.ConfigState.KEY.get(client.world).insaneSeesMorphs) return false;
        return dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(client.player).isLowerThanDepressed();
    }
}