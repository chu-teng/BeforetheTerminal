package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;






@Mixin(MoodRenderer.class)
public abstract class MoodRendererTypeMixin {
    @ModifyExpressionValue(method = "renderHud", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/PlayerPsychoComponent;getPsychoTicks()I"))
    private static int btt$memoryNormalBar(int original) {
        return dev.hae.btt.encounter.Memory.isBlind(MinecraftClient.getInstance().player) ? 0 : original;
    }

    @ModifyExpressionValue(method = "renderHud",
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/api/Role;getMoodType()Ldev/doctor4t/wathe/api/Role$MoodType;"))
    private static Role.MoodType bttMoodTypeOverride(Role.MoodType original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return original;
        if (!SeatAssignment.isBttMode(client.world)) return original;
        return Roles.effectiveMoodType(GameWorldComponent.KEY.get(client.world), client.player);
    }
}
