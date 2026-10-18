package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.component.CorpseState;
import dev.hae.btt.component.DisguiseState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class CorpseDeathReasonMixin {

    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/entity/PlayerBodyEntity;setHeadYaw(F)V"), cancellable = true)
    private static void setDeathReason(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier identifier, CallbackInfo ci, @Local PlayerBodyEntity playerBodyEntity) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.getWorld());
        if (gameWorldComponent.getRole(victim) == null) return;
        Identifier reason = identifier;
        CorpseState corpse = CorpseState.KEY.get(playerBodyEntity);
        corpse.deathReason = reason;
        corpse.technicalRole = gameWorldComponent.getRole(victim).identifier();

        dev.hae.btt.component.PlayerState victimState = dev.hae.btt.component.PlayerState.KEY.get(victim);
        dev.doctor4t.wathe.api.Role first = victimState == null ? null
                : dev.hae.btt.role.Roles.byId(victimState.initialRole);
        corpse.initialRole = first == null ? corpse.technicalRole : first.identifier();
    }

}
