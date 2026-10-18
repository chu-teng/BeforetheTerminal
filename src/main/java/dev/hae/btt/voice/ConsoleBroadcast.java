package dev.hae.btt.voice;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.server.network.ServerPlayerEntity;










public final class ConsoleBroadcast {
    private ConsoleBroadcast() {}


    public static boolean isBroadcasting(ServerPlayerEntity player) {
        if (!SeatAssignment.isBttMode(player.getWorld())) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (gwc == null || !gwc.isRunning()) return false;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return false;
        return PlayerState.KEY.get(player).hasConsoleSession();
    }
}
