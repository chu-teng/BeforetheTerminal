package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Mastermind {
    private Mastermind() {}

    public static void death(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var world = victim.getServerWorld();
        var game = GameWorldComponent.KEY.get(world);
        var state = GameState.KEY.get(world);
        var effects = state.roleEffects;
        var clock = GameTimeComponent.KEY.get(world);
        var owners = world.getPlayers().stream().filter(GameFunctions::isPlayerAliveAndSurvival)
                .filter(p -> Roles.hasCurrentRole(game, p, Roles.MASTERMIND) && !Drunk.isDrunk(p)).toList();
        if (owners.isEmpty()) return;
        if (effects.getBoolean("mastermindWindow") && clock.getTime() > 0
                && Roles.isPassengerCampFor(game, victim) && TraitRules.passengerExecution(victim, killer, reason))
            state.killerObjectiveMet = true;
        if (Roles.kindOfReal(game, victim) != Roles.Kind.PRINCIPAL) return;
        boolean triggered = false;
        for (var owner : owners) {
            String key = "mastermindUsed:" + owner.getUuidAsString();
            if (effects.getBoolean(key)) continue;
            effects.putBoolean(key, true);
            triggered = true;
        }
        if (triggered) {
            effects.putBoolean("mastermindWindow", true);
            clock.setTime(Math.min(clock.getTime(), 2400));
        }
    }
}
