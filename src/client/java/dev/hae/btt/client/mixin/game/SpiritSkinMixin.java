package dev.hae.btt.client.mixin.game;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;








@Mixin(AbstractClientPlayerEntity.class)
public abstract class SpiritSkinMixin {

    @Unique
    private static final SkinTextures BTT_STEVE_SKIN = new SkinTextures(
            Identifier.ofVanilla("textures/entity/player/wide/steve.png"),
            null, null, null,
            SkinTextures.Model.WIDE,
            true);

    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    private void btt$spiritAnonymousSkin(CallbackInfoReturnable<SkinTextures> cir) {
        if (!SpiritCameraHandler.isActive()) return;
        if ((Object) this == MinecraftClient.getInstance().player) return;
        cir.setReturnValue(BTT_STEVE_SKIN);
    }
}