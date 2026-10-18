package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Colonel {
    private Colonel() {}
    private static final ThreadLocal<Boolean> PICKUP = ThreadLocal.withInitial(() -> false);

    public static void toggle(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (state.isDrunk()) return;
        state.colonelFeigning = !state.colonelFeigning;
        state.sync();
    }

    public static boolean feigning(PlayerEntity player) {
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return false;
        var game = GameWorldComponent.KEY.get(player.getWorld());
        var state = PlayerState.KEY.get(player);
        return game.isRunning() && game.getRole(player) == Roles.COLONEL && state.colonelFeigning && !state.isDrunk();
    }

    public static boolean hypocrisy(PlayerEntity player) {
        return player != null && SeatAssignment.isBttMode(player.getWorld())
                && GameWorldComponent.KEY.get(player.getWorld()).isRunning() && dev.hae.btt.encounter.Backgrounds.isActive(player.getWorld(), dev.hae.btt.encounter.Backgrounds.HYPOCRISY);
    }
    public static boolean pickupPrivilege(PlayerEntity player) {
        return feigning(player) || hypocrisy(player) || Decoy.passengerFacade(player);
    }
    public static boolean executionUser(PlayerEntity player, Identifier reason) {
        return player != null && (Decoy.passengerFacade(player) || Roles.isPassengerCampFor(GameWorldComponent.KEY.get(player.getWorld()), player)
                || GameConstants.DeathReasons.GUN.equals(reason) && feigning(player)
                || hypocrisy(player) && (GameConstants.DeathReasons.GUN.equals(reason) || DeathReasons.ARROW.equals(reason)));
    }

    public static boolean inPickup() { return PICKUP.get(); }
    public static boolean beginPickup() { boolean previous = PICKUP.get(); PICKUP.set(true); return previous; }
    public static void endPickup(boolean previous) { if (previous) PICKUP.set(true); else PICKUP.remove(); }
}
