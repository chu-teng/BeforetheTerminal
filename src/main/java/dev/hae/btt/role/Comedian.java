package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.GameState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Comedian {
    private Comedian() {}

    public static void inflate(ServerPlayerEntity caster, ServerPlayerEntity target) {
        var ability = AbilityState.KEY.get(caster);
        if (ability.cooldown > 0 || caster == target || caster.distanceTo(target) > 6) return;
        GameState.KEY.get(caster.getWorld()).roleEffects.putBoolean("inflated:" + target.getUuidAsString(), true);
        ability.setCooldown(RoleDefs.CD_1MIN);
        ability.sync();
    }


    public static boolean executionAttempt(PlayerEntity actor, Identifier reason) {
        if (!(actor instanceof ServerPlayerEntity player)
                || !dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())
                || !GameFunctions.isPlayerAliveAndSurvival(player)
                || !dev.hae.btt.encounter.EncounterRules.execution(reason)
                || !Colonel.executionUser(player, reason)) return false;
        var effects = GameState.KEY.get(player.getWorld()).roleEffects;
        String key = "inflated:" + player.getUuidAsString();
        if (!effects.getBoolean(key)) return false;
        effects.remove(key);
        GameFunctions.killPlayer(player, true, player, DeathReasons.HELIUM_SELF_DESTRUCT);
        return true;
    }
}
