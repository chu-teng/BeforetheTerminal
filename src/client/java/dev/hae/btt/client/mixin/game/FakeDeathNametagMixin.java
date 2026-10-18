package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.hae.btt.component.PlayerState;






@Mixin(targets = "dev.doctor4t.wathe.client.gui.RoleNameRenderer")
public abstract class FakeDeathNametagMixin {

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static Text bttFakeDeathNoName(PlayerEntity instance, Operation<Text> original) {
        if (instance != null && PlayerState.KEY.get(instance).isFakeDead()) return Text.empty();
        return original.call(instance);
    }
}
