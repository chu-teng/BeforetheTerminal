package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.CurseState;
import dev.hae.btt.component.DisguiseState;
import dev.hae.btt.component.ExecutionerState;
import dev.hae.btt.component.ScavengerState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;










@Mixin(GameFunctions.class)
public abstract class PlayerStateResetMixin {

    @Inject(method = "resetPlayer", at = @At("TAIL"))
    private static void btt$resetPlayerState(ServerPlayerEntity player, CallbackInfo ci) {
        DisguiseState.KEY.get(player).reset();
        CurseState.KEY.get(player).reset();
        ScavengerState.KEY.get(player).reset();
        ExecutionerState.KEY.get(player).reset();
        AbilityState.KEY.get(player).reset();


        for (net.minecraft.item.Item item : dev.doctor4t.wathe.game.GameConstants.ITEM_COOLDOWNS.keySet()) {
            player.getItemCooldownManager().remove(item);
        }
        player.getItemCooldownManager().remove(net.minecraft.item.Items.BOW);

        player.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.INVISIBILITY);
    }
}
