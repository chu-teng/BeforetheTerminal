package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.config.BttConfig;
import dev.hae.btt.component.CurseState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class WitchCurseMixin {

    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", at = @At("HEAD"))
    private static void curseOnKill(PlayerEntity victim, boolean spawnBody, PlayerEntity killer, Identifier identifier, CallbackInfo ci) {
        if (BttConfig.HANDLER.instance().curseNonKillerDeaths || killer != null) {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(victim.getWorld());
            if (gameWorldComponent.isRole(victim, dev.hae.btt.role.Roles.WITCH)) {
                CurseState cursePlayerComponent = (CurseState) CurseState.KEY.get(victim);
                if (cursePlayerComponent.target != null) {
                    PlayerEntity curseed = victim.getWorld().getPlayerByUuid(cursePlayerComponent.target);
                    if (curseed != null) {
                        if (GameFunctions.isPlayerAliveAndSurvival(curseed) && curseed != victim) {
                            GameFunctions.killPlayer(curseed, true, null, dev.hae.btt.combat.DeathReasons.MAGIC);
                        }
                    }
                }
            }
        }
    }

}
