package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.network.KnifeStabPayload;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.game.SeatAssignment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KnifeStabPayload.Receiver.class)
public abstract class KnifeUseMixin {
    @WrapMethod(method = "receive")
    private void bttKnifeUse(KnifeStabPayload payload, ServerPlayNetworking.Context context, Operation<Void> original) {
        var player = context.player();
        if (SeatAssignment.isBttMode(player.getWorld())) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player) || !player.getMainHandStack().isOf(WatheItems.KNIFE)
                    || player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)
                    || !(player.getServerWorld().getEntityById(payload.target()) instanceof PlayerEntity target)
                    || target == player || player.distanceTo(target) > 3
                    || !GameFunctions.isPlayerAliveAndSurvival(target)) return;
            if (dev.hae.btt.role.Comedian.executionAttempt(player, dev.doctor4t.wathe.game.GameConstants.DeathReasons.KNIFE)) return;
            var definition = RoleDefs.get(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
            if (definition != null) definition.useKnife(player);
        }
        original.call(payload, context);
    }
}
