package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import dev.hae.btt.game.SeatAssignment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;








@Mixin(targets = "dev.doctor4t.wathe.client.gui.MoodRenderer$TaskRenderer")
public abstract class MoodTaskTextMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/client/WatheClient;isKiller()Z"))
    private boolean bttTaskText(Operation<Boolean> original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !SeatAssignment.isBttMode(client.world)) {
            return original.call();
        }
        Role role = GameWorldComponent.KEY.get(client.world).getRole(client.player);
        if (role != null && dev.hae.btt.role.Roles.effectiveMoodType(
                GameWorldComponent.KEY.get(client.world), client.player) == Role.MoodType.FAKE) return true;
        return original.call();
    }
}
