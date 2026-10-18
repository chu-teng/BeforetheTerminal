package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.util.Identifier;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;





@Mixin(PlayerBodyEntityRenderer.class)
public abstract class SpiritCorpseSkinMixin {

    @Unique
    private static final Identifier BTT_STEVE_TEXTURE =
            Identifier.ofVanilla("textures/entity/player/wide/steve.png");

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void btt$spiritAnonymousCorpse(PlayerBodyEntity body, CallbackInfoReturnable<Identifier> cir) {
        if (SpiritCameraHandler.isActive()) cir.setReturnValue(BTT_STEVE_TEXTURE);
    }
}