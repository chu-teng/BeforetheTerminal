package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.victory.Endings;
import dev.hae.btt.victory.SoloWin;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.LinkedHashSet;
import java.util.List;


public final class KillObjectives {
    private KillObjectives() {}
    public static final int REQUIRED = 3;

    public static void observe(Role role, ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        if (!game.isRunning()) return;
        if (role == Roles.COLD_WAVE && DeathReasons.COLLAPSE.equals(reason)) {
            for (var owner : victim.getServerWorld().getPlayers()) {
                if (game.isRole(owner, role) && owner.isAlive() && GameFunctions.isPlayerAliveAndSurvival(owner))
                    count(owner, victim, Endings.Ending.COLD_WAVE_WIN);
            }
        } else if (killer != null && game.isRole(killer, role)) {
            if (role == Roles.ASTRAL && DeathReasons.MAGIC.equals(reason)) count(killer, victim, Endings.Ending.ASTRAL_WIN);
            if (role == Roles.KIDNAPPER && DeathReasons.DIGESTED.equals(reason)) count(killer, victim, Endings.Ending.KIDNAPPER_WIN);
        }
    }

    private static void count(ServerPlayerEntity owner, ServerPlayerEntity victim, Endings.Ending ending) {
        var state = PlayerState.KEY.get(owner);
        var ids = new LinkedHashSet<>(state.objectiveVictims.isEmpty() ? List.<String>of() : List.of(state.objectiveVictims.split(",")));
        if (!ids.add(victim.getUuidAsString())) return;
        state.objectiveVictims = String.join(",", ids);
        state.sync();
        if (ids.size() >= REQUIRED) SoloWin.request(owner.getServerWorld(), ending, owner);
    }
}
