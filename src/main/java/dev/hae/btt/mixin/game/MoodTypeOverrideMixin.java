package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;










@Mixin(PlayerMoodComponent.class)
public abstract class MoodTypeOverrideMixin {

    @Shadow @Final private PlayerEntity player;

    @ModifyExpressionValue(method = {"getMood", "setMood"},
            at = @At(value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/api/Role;getMoodType()Ldev/doctor4t/wathe/api/Role$MoodType;"))
    private Role.MoodType bttMoodTypeOverride(Role.MoodType original) {
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return original;
        return Roles.effectiveMoodType(GameWorldComponent.KEY.get(player.getWorld()), player);
    }
}
