package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.CurseState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameFunctions.class)
public abstract class WitchCurseMixin {
    @WrapMethod(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V")
    private static void curseAfterDeath(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason, Operation<Void> original) {
        boolean alive = GameFunctions.isPlayerAliveAndSurvival(victim);
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        boolean eligible = SeatAssignment.isBttMode(victim.getWorld()) && game.isRunning()
                && game.isRole(victim, Roles.WITCH) && !DeathReasons.isSuicide(reason)
                && !PlayerState.KEY.get(victim).isDrunk();
        var id = eligible ? CurseState.KEY.get(victim).target : null;
        original.call(victim, body, killer, reason);
        if (!alive || GameFunctions.isPlayerAliveAndSurvival(victim) || !eligible) return;
        var target = id == null ? null : victim.getWorld().getPlayerByUuid(id);
        if (target != victim && GameFunctions.isPlayerAliveAndSurvival(target)) GameFunctions.killPlayer(target, true, victim, DeathReasons.MAGIC);
    }
}
