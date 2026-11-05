package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;


public final class Werewolf {
    private Werewolf() {}
    public static boolean deceives(GameWorldComponent game, PlayerEntity observer, PlayerEntity target) {
        if (!Roles.isPassengerCampFor(game, observer)) return false;
        var kind = Roles.kindOfReal(game, target);
        if (kind == null || !(kind.isNeutralKind() || kind.isKillerSeat())) return false;
        return active(game, observer);
    }
    public static boolean active(GameWorldComponent game, PlayerEntity observer) {
        if (!Roles.isPassengerCampFor(game, observer)) return false;
        return observer.getWorld().getPlayers().stream().anyMatch(p -> game.isRole(p, Roles.WEREWOLF)
                && GameFunctions.isPlayerAliveAndSurvival(p) && !Drunk.isDrunk(p));
    }
    public static Roles.Kind kind(GameWorldComponent game, PlayerEntity observer, PlayerEntity target) {
        if (!deceives(game, observer, target)) return Roles.kindOfReal(game, target);
        return (target.getUuid().getLeastSignificantBits() & 1) == 0 ? Roles.Kind.PRINCIPAL : Roles.Kind.ACCOMPLICE;
    }

    public static boolean matchesKind(GameWorldComponent game, PlayerEntity observer, PlayerEntity target, Roles.Kind queried) {
        return deceives(game, observer, target) && queried.isKillerSeat()
                || kind(game, observer, target) == queried;
    }
    public static boolean passenger(GameWorldComponent game, PlayerEntity observer, PlayerEntity target) {
        return !deceives(game, observer, target) && Roles.isPassengerCampFor(game, target);
    }
}
