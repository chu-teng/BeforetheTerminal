package dev.hae.btt.mixin.game;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.network.GunShootPayload;
import dev.hae.btt.advancement.BttAdvancements;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(GunShootPayload.Receiver.class)
public abstract class AdvancementGunMixin {
    @WrapMethod(method = "receive")
    private void bttShot(GunShootPayload payload, ServerPlayNetworking.Context context, Operation<Void> original) {
        var player = context.player(); var stack = player.getMainHandStack();
        boolean ready = stack.isOf(WatheItems.DERRINGER) && !player.getItemCooldownManager().isCoolingDown(WatheItems.DERRINGER)
                && !Boolean.TRUE.equals(stack.get(WatheDataComponentTypes.USED));
        boolean hit = player.getWorld().getEntityById(payload.target()) instanceof PlayerEntity target
                && GameFunctions.isPlayerAliveAndSurvival(target) && target.distanceTo(player) < 65;
        original.call(payload, context);
        if (ready && (player.isCreative() || Boolean.TRUE.equals(stack.get(WatheDataComponentTypes.USED))
                || player.getItemCooldownManager().isCoolingDown(WatheItems.DERRINGER))) BttAdvancements.derringer(player, hit);
    }
}
