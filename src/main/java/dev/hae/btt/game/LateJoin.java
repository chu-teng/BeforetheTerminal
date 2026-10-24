package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;


public final class LateJoin {
    private LateJoin() {}

    private static boolean active(ServerWorld world) {
        var game = GameWorldComponent.KEY.get(world);
        return SeatAssignment.isBttMode(world) && game.isRunning()
                && !game.getRoles().isEmpty();
    }

    public static void placeSpectator(ServerPlayerEntity player) {
        var server = player.getServer();
        if (server == null) return;
        RoundResults.apply(player.getServerWorld());
        ServerWorld destination = active(player.getServerWorld()) ? player.getServerWorld() : null;
        if (destination == null) for (var world : server.getWorlds()) {
            if (active(world)) { destination = world; break; }
        }
        if (destination == null || GameWorldComponent.KEY.get(destination).getRole(player) != null) return;
        player.changeGameMode(GameMode.SPECTATOR);
        var spawn = MapVariablesWorldComponent.KEY.get(destination).getSpectatorSpawnPos();
        player.teleport(destination, spawn.pos.x, spawn.pos.y, spawn.pos.z, spawn.yaw, spawn.pitch);
    }
}
