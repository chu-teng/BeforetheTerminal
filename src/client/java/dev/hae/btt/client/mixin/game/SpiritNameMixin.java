package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.client.spirit.SpiritCameraHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;







@Mixin(RoleNameRenderer.class)
public abstract class SpiritNameMixin {

    @WrapOperation(method = "renderHud",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static Text btt$spiritHideName(PlayerEntity instance, Operation<Text> original) {
        if (SpiritCameraHandler.isAnonymous() && instance != MinecraftClient.getInstance().player) {
            return Text.literal("");
        }

        if (dev.hae.btt.component.PlayerState.KEY.get(instance).isSwallowed()) {
            return Text.literal("");
        }
        return original.call(instance);
    }
}