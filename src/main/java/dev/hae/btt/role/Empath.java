package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class Empath {
    private Empath() {}

    public static void tick(ServerPlayerEntity player, ServerWorld world, GameWorldComponent game) {
        if (!GameFunctions.isPlayerAliveAndSurvival(player) || Drunk.isDrunk(player)) return;
        long nearby = world.getPlayers().stream().filter(p -> p != player)
                .filter(GameFunctions::isPlayerAliveAndSurvival)
                .filter(p -> player.squaredDistanceTo(p) <= 400 && game.getRole(p) != null)
                .filter(p -> !Roles.isPassengerCampFor(game, p)).count();
        if (nearby >= 4) {
            GameFunctions.killPlayer(player, true, null, DeathReasons.COLLAPSE);
        } else if (nearby > 0) {
            var mood = PlayerMoodComponent.KEY.get(player);
            mood.setMood(mood.getMood() - nearby * dev.hae.btt.game.Needs.drainingCount(player) * GameConstants.MOOD_DRAIN);
        }
    }
}
