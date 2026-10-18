package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.hae.btt.component.ConfigState;
import dev.hae.btt.client.BttClient;
import dev.hae.btt.component.DisguiseState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RoleNameRenderer.class)
public abstract class DisguiseRoleNameRendererMixin {

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private static Text renderRoleHud(PlayerEntity instance, Operation<Text> original) {

        if (WatheClient.moodComponent != null) {
            if ((ConfigState.KEY.get(instance.getWorld())).insaneSeesMorphs && WatheClient.moodComponent.isLowerThanDepressed() && BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(instance.getUuid()) != null) {
                return Text.literal("??!?!").formatted(Formatting.OBFUSCATED);
            }
        }
        if (instance.isInvisible()) {
            return Text.literal("");
        }
        if ((DisguiseState.KEY.get(instance)).getMorphTicks() > 0) {
            if (instance.getWorld().getPlayerByUuid(DisguiseState.KEY.get(instance).disguise) != null) {
                return instance.getWorld().getPlayerByUuid((DisguiseState.KEY.get(instance)).disguise).getDisplayName();
            } else {
                Log.info(LogCategory.GENERAL, "Morphling disguise is null!!!");
            }
            if (DisguiseState.KEY.get(instance).disguise.equals(MinecraftClient.getInstance().player.getUuid())) {
                return MinecraftClient.getInstance().player.getDisplayName();
            }
        }
        return original.call(instance);
    }

}
